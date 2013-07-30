/*
 * Copyright (C) 2005-2013 Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.server.jar.patch;

import com.stimulus.archiva.update.core.codec.JaxbCodec;
import com.stimulus.archiva.update.core.io.*;
import com.stimulus.archiva.update.server.jar.commons.EntrySource;
import com.stimulus.archiva.update.server.jar.model.*;
import com.stimulus.archiva.update.server.util.MessageDigests;
import java.io.*;
import static java.util.Objects.requireNonNull;
import java.security.*;
import java.util.SortedMap;
import java.util.jar.*;
import java.util.zip.*;
import javax.annotation.*;
import javax.annotation.concurrent.NotThreadSafe;
import javax.xml.bind.*;

/**
 * Applies a JAR diff file to an input JAR file.
 *
 * @author Christian Schlichtherle
 */
@NotThreadSafe
public abstract class JarPatch {

    private Diff diff;

    /** Returns the JAR diff file. */
    abstract @WillNotClose ZipFile jarDiffFile();

    /** Returns the input JAR file. */
    abstract @WillNotClose JarFile inputJarFile();

    /** Returns the JAXB context for unmarshalling the JAR diff bean. */
    abstract JAXBContext jaxbContext();

    /**
     * Applies the configured JAR diff file.
     *
     * @param outputJarFile the sink for writing the output JAR file.
     */
    public final void applyDiffFileTo(final Sink outputJarFile)
    throws IOException {
        try (JarOutputStream out = new JarOutputStream(outputJarFile.output())) {
            // The JarInputStream class assumes that the file entry
            // "META-INF/MANIFEST.MF" should be either the first or the second
            // entry (if preceded by the directory entry "META-INF/"), so we
            // need to process the JAR diff file in two passes with a
            // corresponding filter to ensure this order.
            // Note that the directory entry "META-INF/" is always part of the
            // unchanged map because it's content is always empty.
            // Thus, by copying the unchanged entries before the changed
            // entries, the directory entry "META-INF/" will always appear
            // before the file entry "META-INF/MANIFEST.MF".
            final Filter manifestFilter = new ManifestFilter();
            applyDiffFileTo(manifestFilter, out);
            applyDiffFileTo(new InverseFilter(manifestFilter), out);
        }
    }

    private void applyDiffFileTo(
            final Filter filter,
            final @WillNotClose JarOutputStream out)
    throws IOException {

        final class EntrySink implements Sink {

            final EntryDigest entryDigest;

            EntrySink(final EntryDigest entryDigest) {
                this.entryDigest = entryDigest;
            }

            @Override public OutputStream output() throws IOException {
                out.putNextEntry(new JarEntry(entryDigest.name));
                return new DigestOutputStream(out, messageDigest()) {

                    { digest.reset(); }

                    @Override public void close() throws IOException {
                        ((JarOutputStream) out).closeEntry();
                        if (!digestToHexString().equals(entryDigest.digest))
                            throw new WrongMessageDigestException(
                                    entryDigest.name);
                    }

                    String digestToHexString() {
                        return MessageDigests.hexString(digest.digest());
                    }
                };
            }
        } // EntrySink

        abstract class Selection<T> {

            abstract EntryDigest apply(T t);
        } // Selection

        final class EntryDigestSelection extends Selection<EntryDigest> {

            @Override EntryDigest apply(EntryDigest entryDigest) {
                return entryDigest;
            }
        } // EntryDigestSelection

        final class FirstAndSecondEntryDigestSelection
                extends Selection<FirstAndSecondEntryDigest> {

            @Override
            EntryDigest apply(FirstAndSecondEntryDigest firstAndSecondEntryDigest) {
                return firstAndSecondEntryDigest.secondEntryDigest();
            }
        } // FirstAndSecondEntryDigestSelection

        abstract class CopyToOutputJarFile {

            abstract ZipFile source();

            abstract IOException ioException(Throwable cause);

            final <T> CopyToOutputJarFile copy(
                    final Selection<T> selection,
                    final @CheckForNull SortedMap<String, T> subject)
            throws IOException {
                if (null == subject) return this;
                for (final T item : subject.values()) {
                    final EntryDigest entryDigest = selection.apply(item);
                    final String name = entryDigest.name;
                    final ZipEntry entry = source().getEntry(name);
                    if (null == entry)
                        throw ioException(new MissingJarEntryException(name));
                    try {
                        copyIfAcceptedByFilter(
                                new EntrySource(entry, source()),
                                new EntrySink(entryDigest));
                    } catch (WrongMessageDigestException ex) {
                        throw ioException(ex);
                    }
                }
                return this;
            }

            final void copyIfAcceptedByFilter(EntrySource source, EntrySink sink)
            throws IOException {
                if (filter.accept(source)) Copy.copy(source, sink);
            }
        } // CopyToOutputJarFile

        final class CopyFromInputJarFile extends CopyToOutputJarFile {

            @Override ZipFile source() { return inputJarFile(); }

            @Override IOException ioException(Throwable cause) {
                return new WrongInputJarFile(source().getName(), cause);
            }
        } // CopyFromInputJarFile

        final class CopyFromJarDiffFile extends CopyToOutputJarFile {

            @Override ZipFile source() { return jarDiffFile(); }

            @Override IOException ioException(Throwable cause) {
                return new InvalidJarDiffFileException(source().getName(), cause);
            }
        } // CopyFromJarDiffFile

        new CopyFromInputJarFile().copy(new EntryDigestSelection(),
                diff().unchanged);
        new CopyFromJarDiffFile().copy(new EntryDigestSelection(),
                diff().added);
        new CopyFromJarDiffFile().copy(new FirstAndSecondEntryDigestSelection(),
                diff().changed);
    }

    /**
     * Returns a message digest for comparison with the mutable JAR diff bean.
     */
    private MessageDigest messageDigest() throws IOException {
        return MessageDigests.newDigest(diff().algorithm);
    }

    /** Returns the mutable JAR diff bean. */
    private Diff diff() throws IOException {
        final Diff diff = this.diff;
        return null != diff ? diff : (this.diff = loadDiff());
    }

    private Diff loadDiff() throws IOException {
        final ZipEntry entry = jarDiffFile().getEntry(Diffs.DIFF_ENTRY_NAME);
        if (null == entry)
            throw new IOException(Diffs.DIFF_ENTRY_NAME + " (entry not found)");
        try {
            return new JaxbCodec(jaxbContext()).decode(
                    new EntrySource(entry, jarDiffFile()), Diff.class);
        } catch (IOException | RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IOException(ex);
        }
    }

    /**
     * A builder for a JAR patch.
     * The default JAXB context binds only the {@link Diff} class.
     */
    public static class Builder {

        private ZipFile jarDiffFile;
        private JarFile inputJarFile;
        private JAXBContext jaxbContext;

        public Builder jarDiffFile(final ZipFile jarDiffFile) {
            this.jarDiffFile = requireNonNull(jarDiffFile);
            return this;
        }

        public Builder inputJarFile(final JarFile inputJarFile) {
            this.inputJarFile = requireNonNull(inputJarFile);
            return this;
        }

        public Builder jaxbContext(final JAXBContext jaxbContext) {
            this.jaxbContext = requireNonNull(jaxbContext);
            return this;
        }

        public JarPatch build() {
            return build(jarDiffFile, inputJarFile,
                    null != jaxbContext ? jaxbContext : Diffs.jaxbContext());
        }

        private static JarPatch build(
                final ZipFile jarDiffFile,
                final JarFile inputJarFile,
                final JAXBContext jaxbContext) {
            requireNonNull(jarDiffFile);
            requireNonNull(inputJarFile);
            assert null != jaxbContext;
            return new JarPatch() {
                @Override ZipFile jarDiffFile() { return jarDiffFile; }
                @Override JarFile inputJarFile() { return inputJarFile; }
                @Override JAXBContext jaxbContext() { return jaxbContext; }
            };
        }
    } // Builder
}

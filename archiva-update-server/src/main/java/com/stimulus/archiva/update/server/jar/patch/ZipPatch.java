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
 * Applies a ZIP patch file to an input ZIP file.
 *
 * @author Christian Schlichtherle
 */
@NotThreadSafe
public abstract class ZipPatch {

    private Diff diff;

    /** Returns the ZIP patch file. */
    abstract @WillNotClose ZipFile zipPatchFile();

    /** Returns the input ZIP file. */
    abstract @WillNotClose ZipFile inputZipFile();

    /** Returns the JAXB context for unmarshalling the ZIP diff bean. */
    abstract JAXBContext jaxbContext();

    /**
     * Applies the configured ZIP diff file.
     *
     * @param outputZipFile the sink for writing the output ZIP file.
     */
    public final void applyDiffFileTo(final Sink outputZipFile)
    throws IOException {
        try (ZipOutputStream out = newZipOutputStream(outputZipFile)) {
            for (final Filter filter : filters())
                applyDiffFileTo(filter, out);
        }
    }

    ZipOutputStream newZipOutputStream(final Sink outputZipFile)
    throws IOException {
        return new JarOutputStream(outputZipFile.output());
    }

    ZipEntry newZipEntry(String name) {
        return new JarEntry(name);
    }

    Filter[] filters() {
        // The JarInputStream class assumes that the file entry
        // "META-INF/MANIFEST.MF" should be either the first or the second
        // entry (if preceded by the directory entry "META-INF/"), so we
        // need to process the ZIP patch file in two passes with a
        // corresponding filter to ensure this order.
        // Note that the directory entry "META-INF/" is always part of the
        // unchanged map because it's content is always empty.
        // Thus, by copying the unchanged entries before the changed
        // entries, the directory entry "META-INF/" will always appear
        // before the file entry "META-INF/MANIFEST.MF".
        final Filter manifestFilter = new ManifestFilter();
        return new Filter[] {
                manifestFilter,
                new InverseFilter(manifestFilter)
        };
    }

    private void applyDiffFileTo(
            final Filter filter,
            final @WillNotClose ZipOutputStream out)
    throws IOException {

        class EntrySink implements Sink {

            final EntryNameWithDigest entryDigest;

            EntrySink(final EntryNameWithDigest entryDigest) {
                this.entryDigest = entryDigest;
            }

            @Override public OutputStream output() throws IOException {
                out.putNextEntry(newZipEntry(entryDigest.name));
                return new DigestOutputStream(out, messageDigest()) {

                    { digest.reset(); }

                    @Override public void close() throws IOException {
                        ((ZipOutputStream) out).closeEntry();
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

        abstract class PatchSet {

            abstract ZipFile source();

            abstract IOException ioException(Throwable cause);

            final <T> PatchSet apply(
                    final Transformation<T> transformation,
                    final @CheckForNull SortedMap<String, T> selection)
            throws IOException {
                if (null == selection) return this;
                for (final T item : selection.values()) {
                    final EntryNameWithDigest
                            entryNameWithDigest = transformation.apply(item);
                    final String name = entryNameWithDigest.name;
                    final ZipEntry entry = source().getEntry(name);
                    if (null == entry)
                        throw ioException(new MissingZipEntryException(name));
                    try {
                        copyIfAcceptedByFilter(
                                new EntrySource(entry, source()),
                                new EntrySink(entryNameWithDigest));
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
        } // PatchSet

        class InputZipFilePatchSet extends PatchSet {

            @Override ZipFile source() { return inputZipFile(); }

            @Override IOException ioException(Throwable cause) {
                return new WrongInputZipFile(source().getName(), cause);
            }
        } // InputZipFilePatchSet

        class ZipPatchFilePatchSet extends PatchSet {

            @Override ZipFile source() { return zipPatchFile(); }

            @Override IOException ioException(Throwable cause) {
                return new InvalidZipPatchFileException(source().getName(), cause);
            }
        } // ZipPatchFilePatchSet

        new InputZipFilePatchSet().apply(
                new IdentityTransformation(),
                diff().unchanged);
        new ZipPatchFilePatchSet().apply(
                new EntryNameWithTwoDigestsTransformation(),
                diff().changed);
        new ZipPatchFilePatchSet().apply(
                new IdentityTransformation(),
                diff().added);
    }

    private MessageDigest messageDigest() throws IOException {
        return MessageDigests.newDigest(diff().algorithm);
    }

    private Diff diff() throws IOException {
        final Diff diff = this.diff;
        return null != diff ? diff : (this.diff = loadDiff());
    }

    private Diff loadDiff() throws IOException {
        final ZipEntry entry = zipPatchFile().getEntry(Diffs.DIFF_ENTRY_NAME);
        if (null == entry)
            throw new InvalidZipPatchFileException(zipPatchFile().getName(),
                    new MissingZipEntryException(Diffs.DIFF_ENTRY_NAME));
        try {
            return new JaxbCodec(jaxbContext()).decode(
                    new EntrySource(entry, zipPatchFile()), Diff.class);
        } catch (IOException | RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new InvalidZipPatchFileException(zipPatchFile().getName(), ex);
        }
    }

    /**
     * A builder for a ZIP patch.
     * The default JAXB context binds only the {@link Diff} class.
     */
    public static class Builder {

        private ZipFile zipPatchFile;
        private ZipFile inputZipFile;
        private JAXBContext jaxbContext;

        public Builder zipPatchFile(final ZipFile zipPatchFile) {
            this.zipPatchFile = requireNonNull(zipPatchFile);
            return this;
        }

        public Builder inputZipFile(final ZipFile inputZipFile) {
            this.inputZipFile = requireNonNull(inputZipFile);
            return this;
        }

        @Deprecated
        public Builder jaxbContext(final JAXBContext jaxbContext) {
            this.jaxbContext = requireNonNull(jaxbContext);
            return this;
        }

        public ZipPatch build() {
            return build(zipPatchFile, inputZipFile,
                    null != jaxbContext ? jaxbContext : Diffs.jaxbContext());
        }

        private static ZipPatch build(
                final ZipFile zipPatchFile,
                final ZipFile inputZipFile,
                final JAXBContext jaxbContext) {
            requireNonNull(zipPatchFile);
            requireNonNull(inputZipFile);
            assert null != jaxbContext;
            return new ZipPatch() {
                @Override ZipFile zipPatchFile() { return zipPatchFile; }
                @Override ZipFile inputZipFile() { return inputZipFile; }
                @Override JAXBContext jaxbContext() { return jaxbContext; }
            };
        }
    } // Builder
}

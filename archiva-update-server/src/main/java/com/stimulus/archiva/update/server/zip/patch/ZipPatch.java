/*
 * Copyright (C) 2005-2013 Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.server.zip.patch;

import com.stimulus.archiva.update.core.codec.JaxbCodec;
import com.stimulus.archiva.update.core.io.*;
import com.stimulus.archiva.update.core.zip.commons.EntrySource;
import com.stimulus.archiva.update.core.zip.model.Diff;
import com.stimulus.archiva.update.core.zip.model.Diffs;
import com.stimulus.archiva.update.core.zip.model.EntryNameWithDigest;
import com.stimulus.archiva.update.core.digest.MessageDigests;
import java.io.*;
import static java.util.Objects.requireNonNull;
import java.security.*;
import java.util.SortedMap;
import java.util.zip.*;
import javax.annotation.*;
import javax.annotation.concurrent.NotThreadSafe;
import javax.xml.bind.*;

/**
 * Applies a ZIP patch file to an input ZIP file and writes an output ZIP file.
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
        final Filter[] passFilters = passFilters();
        if (null == passFilters || 0 >= passFilters.length)
            throw new IllegalStateException("At least one pass filter is required to output anything.");
        try (ZipOutputStream out = newZipOutputStream(outputZipFile)) {
            for (Filter filter : passFilters)
                applyDiffFileTo(filter, out);
        }
    }

    /** Returns a new ZIP output stream which writes to the given sink. */
    ZipOutputStream newZipOutputStream(Sink outputZipFile) throws IOException {
        return new ZipOutputStream(outputZipFile.output());
    }

    /** Returns a new ZIP entry with the given name. */
    ZipEntry newZipEntry(String name) { return new ZipEntry(name); }

    /**
     * Returns a list of filters for the different passes required to process
     * the output ZIP file.
     * At least one filter is required to output anything.
     * The filters should properly partition the set of entry sources,
     * i.e. each entry source should be accepted by exactly one filter.
     */
    Filter[] passFilters() { return new Filter[] { new AcceptAllFilter() }; }

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

        // Order is important here!
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

        private ZipFile zipPatchFile, inputZipFile;
        private boolean outputJarFile;
        private JAXBContext jaxbContext;

        public Builder zipPatchFile(final ZipFile zipPatchFile) {
            this.zipPatchFile = requireNonNull(zipPatchFile);
            return this;
        }

        public Builder inputZipFile(final ZipFile inputZipFile) {
            this.inputZipFile = requireNonNull(inputZipFile);
            return this;
        }

        public Builder outputJarFile(final boolean outputJarFile) {
            this.outputJarFile = outputJarFile;
            return this;
        }

        @Deprecated
        public Builder jaxbContext(final JAXBContext jaxbContext) {
            this.jaxbContext = requireNonNull(jaxbContext);
            return this;
        }

        public ZipPatch build() {
            return build(zipPatchFile, inputZipFile, outputJarFile,
                    null != jaxbContext ? jaxbContext : Diffs.jaxbContext()
            );
        }

        private static ZipPatch build(
                final ZipFile zipPatchFile,
                final ZipFile inputZipFile,
                final boolean outputJarFile,
                final JAXBContext jaxbContext) {
            requireNonNull(zipPatchFile);
            requireNonNull(inputZipFile);
            assert null != jaxbContext;
            if (outputJarFile) {
                return new JarPatch() {
                    @Override ZipFile zipPatchFile() { return zipPatchFile; }
                    @Override ZipFile inputZipFile() { return inputZipFile; }
                    @Override JAXBContext jaxbContext() { return jaxbContext; }
                };
            } else {
                return new ZipPatch() {
                    @Override ZipFile zipPatchFile() { return zipPatchFile; }
                    @Override ZipFile inputZipFile() { return inputZipFile; }
                    @Override JAXBContext jaxbContext() { return jaxbContext; }
                };
            }
        }
    } // Builder
}

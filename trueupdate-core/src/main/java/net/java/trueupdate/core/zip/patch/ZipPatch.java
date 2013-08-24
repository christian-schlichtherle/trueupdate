/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.core.zip.patch;

import java.io.*;
import java.security.*;
import java.util.Collection;
import static java.util.Objects.requireNonNull;
import java.util.zip.*;
import javax.annotation.*;
import javax.annotation.concurrent.NotThreadSafe;
import net.java.trueupdate.core.io.*;
import net.java.trueupdate.core.util.*;
import net.java.trueupdate.core.zip.model.*;

/**
 * Applies a ZIP patch file to an input ZIP file and writes an output ZIP file.
 *
 * @author Christian Schlichtherle
 */
@NotThreadSafe
public abstract class ZipPatch {

    private ZipDiffModel model;

    /** Returns the ZIP patch file. */
    abstract @WillNotClose ZipFile zipPatchFile();

    /** Returns the input ZIP file. */
    abstract @WillNotClose ZipFile inputZipFile();

    /** Returns a new builder for a ZIP patch. */
    public static Builder builder() { return new Builder(); }

    /**
     * Applies the configured ZIP patch file.
     *
     * @param outputZipFile the sink for writing the output ZIP or JAR file.
     */
    public final void applyZipPatchFileTo(final Sink outputZipFile)
    throws IOException {
        final ZipEntryNameFilter[] passFilters = passFilters();
        if (null == passFilters || 0 >= passFilters.length)
            throw new IllegalStateException("At least one pass filter is required to output anything.");
        try (ZipOutputStream out = newZipOutputStream(outputZipFile)) {
            for (ZipEntryNameFilter filter : passFilters)
                applyZipPatchFileTo(filter, out);
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
    ZipEntryNameFilter[] passFilters() { return new ZipEntryNameFilter[] { new AcceptAllZipEntryNameFilter() }; }

    private void applyZipPatchFileTo(
            final ZipEntryNameFilter filter,
            final @WillNotClose ZipOutputStream out)
    throws IOException {

        final MessageDigest messageDigest = messageDigest();

        final class ZipEntrySink implements Sink {

            final ZipEntryNameAndDigestValue zipEntryNameAndDigestValue;

            ZipEntrySink(final ZipEntryNameAndDigestValue
                                 zipEntryNameAndDigestValue) {
                assert null != zipEntryNameAndDigestValue;
                this.zipEntryNameAndDigestValue = zipEntryNameAndDigestValue;
            }

            @Override public OutputStream output() throws IOException {
                final ZipEntry entry =
                        newZipEntry(zipEntryNameAndDigestValue.entryName());
                if (entry.isDirectory()) {
                    entry.setMethod(ZipOutputStream.STORED);
                    entry.setSize(0);
                    entry.setCompressedSize(0);
                    entry.setCrc(0);
                }
                out.putNextEntry(entry);
                messageDigest.reset();
                return new DigestOutputStream(out, messageDigest) {

                    @Override public void close() throws IOException {
                        ((ZipOutputStream) out).closeEntry();
                        if (!valueOfDigest().equals(
                                zipEntryNameAndDigestValue.digestValue()))
                            throw new WrongMessageDigestException(
                                    zipEntryNameAndDigestValue.entryName());
                    }

                    String valueOfDigest() {
                        return MessageDigests.valueOf(digest);
                    }
                };
            }
        } // ZipEntrySink

        abstract class PatchSet {

            abstract ZipFile source();

            abstract IOException ioException(Throwable cause);

            final <T> PatchSet apply(
                    final Transformation<T> transformation,
                    final Collection<T> collection)
            throws IOException {
                for (final T item : collection) {
                    final ZipEntryNameAndDigestValue
                            zipEntryNameAndDigestValue = transformation.apply(item);
                    final String name = zipEntryNameAndDigestValue.entryName();
                    if (!filter.accept(name)) continue;
                    final ZipEntry entry = source().getEntry(name);
                    if (null == entry)
                        throw ioException(new MissingZipEntryException(name));
                    try {
                        Copy.copy(
                                new ZipEntrySource(entry, source()),
                                new ZipEntrySink(zipEntryNameAndDigestValue));
                    } catch (WrongMessageDigestException ex) {
                        throw ioException(ex);
                    }
                }
                return this;
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
                zipDiffModel().unchangedEntries());
        new ZipPatchFilePatchSet().apply(
                new ZipEntryNameWithTwoDigestsTransformation(),
                zipDiffModel().changedEntries());
        new ZipPatchFilePatchSet().apply(
                new IdentityTransformation(),
                zipDiffModel().addedEntries());
    }

    private MessageDigest messageDigest() throws IOException {
        return MessageDigests.create(zipDiffModel().messageDigestAlgorithmName());
    }

    private ZipDiffModel zipDiffModel() throws IOException {
        final ZipDiffModel model = this.model;
        return null != model ? model : (this.model = loadZipDiffModel());
    }

    private ZipDiffModel loadZipDiffModel() throws IOException {
        try {
            return ZipDiffModel.decodeFromXml(
                    new ZipEntrySource(zipDiffModelEntry(), zipPatchFile()));
        } catch (IOException | RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new InvalidZipPatchFileException(zipPatchFile().getName(), ex);
        }
    }

    private ZipEntry zipDiffModelEntry() throws IOException {
        final String name = ZipDiffModel.ENTRY_NAME;
        final ZipEntry entry = zipPatchFile().getEntry(name);
        if (null == entry)
            throw new InvalidZipPatchFileException(zipPatchFile().getName(),
                    new MissingZipEntryException(name));
        return entry;
    }

    /** A builder for a ZIP patch. */
    public static final class Builder {

        private @CheckForNull ZipFile zipPatchFile, inputZipFile;
        private boolean outputJarFile;

        Builder() { }

        public Builder zipPatchFile(final @Nullable ZipFile zipPatchFile) {
            this.zipPatchFile = zipPatchFile;
            return this;
        }

        public Builder inputZipFile(final @Nullable ZipFile inputZipFile) {
            this.inputZipFile = inputZipFile;
            return this;
        }

        public Builder outputJarFile(final boolean outputJarFile) {
            this.outputJarFile = outputJarFile;
            return this;
        }

        public ZipPatch build() {
            return create(zipPatchFile, inputZipFile, outputJarFile);
        }

        private static ZipPatch create(
                final ZipFile zipPatchFile,
                final ZipFile inputZipFile,
                final boolean outputJarFile) {
            requireNonNull(zipPatchFile);
            requireNonNull(inputZipFile);
            if (outputJarFile) {
                return new JarPatch() {
                    @Override ZipFile zipPatchFile() { return zipPatchFile; }
                    @Override ZipFile inputZipFile() { return inputZipFile; }
                };
            } else {
                return new ZipPatch() {
                    @Override ZipFile zipPatchFile() { return zipPatchFile; }
                    @Override ZipFile inputZipFile() { return inputZipFile; }
                };
            }
        }
    } // Builder
}

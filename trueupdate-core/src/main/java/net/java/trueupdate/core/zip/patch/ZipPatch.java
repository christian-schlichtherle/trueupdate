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

    private DiffModel model;

    /** Returns the ZIP patch file. */
    abstract @WillNotClose ZipFile zipPatchFile();

    /** Returns the input ZIP file. */
    abstract @WillNotClose ZipFile inputZipFile();

    /**
     * Applies the configured ZIP patch file.
     *
     * @param outputZipFile the sink for writing the output ZIP file.
     */
    public final void applyZipPatchFileTo(final Sink outputZipFile)
    throws IOException {
        final Filter[] passFilters = passFilters();
        if (null == passFilters || 0 >= passFilters.length)
            throw new IllegalStateException("At least one pass filter is required to output anything.");
        try (ZipOutputStream out = newZipOutputStream(outputZipFile)) {
            for (Filter filter : passFilters)
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
    Filter[] passFilters() { return new Filter[] { new AcceptAllFilter() }; }

    private void applyZipPatchFileTo(
            final Filter filter,
            final @WillNotClose ZipOutputStream out)
    throws IOException {

        class EntrySink implements Sink {

            final EntryNameAndDigest entryNameAndDigest;

            EntrySink(final EntryNameAndDigest entryNameAndDigest) {
                this.entryNameAndDigest = entryNameAndDigest;
            }

            @Override public OutputStream output() throws IOException {
                out.putNextEntry(newZipEntry(entryNameAndDigest.name()));
                return new DigestOutputStream(out, messageDigest()) {

                    { digest.reset(); }

                    @Override public void close() throws IOException {
                        ((ZipOutputStream) out).closeEntry();
                        if (!digestToHexString().equals(entryNameAndDigest.digest()))
                            throw new WrongMessageDigestException(
                                    entryNameAndDigest.name());
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
                    final Collection<T> collection)
            throws IOException {
                for (final T item : collection) {
                    final EntryNameAndDigest
                            entryNameAndDigest = transformation.apply(item);
                    final String name = entryNameAndDigest.name();
                    final ZipEntry entry = source().getEntry(name);
                    if (null == entry)
                        throw ioException(new MissingZipEntryException(name));
                    try {
                        copyIfAcceptedByFilter(
                                new EntrySource(entry, source()),
                                new EntrySink(entryNameAndDigest));
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
                diffModel().unchangedEntries());
        new ZipPatchFilePatchSet().apply(
                new EntryNameWithTwoDigestsTransformation(),
                diffModel().changedEntries());
        new ZipPatchFilePatchSet().apply(
                new IdentityTransformation(),
                diffModel().addedEntries());
    }

    private MessageDigest messageDigest() throws IOException {
        return MessageDigests.newDigest(diffModel().messageDigestAlgorithmName());
    }

    private DiffModel diffModel() throws IOException {
        final DiffModel model = this.model;
        return null != model ? model : (this.model = loadDiffModel());
    }

    private DiffModel loadDiffModel() throws IOException {
        try {
            return DiffModel.decodeFromXml(
                    new EntrySource(diffModelEntry(), zipPatchFile()));
        } catch (IOException | RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new InvalidZipPatchFileException(zipPatchFile().getName(), ex);
        }
    }

    private ZipEntry diffModelEntry() throws IOException {
        final String name = DiffModel.ENTRY_NAME;
        final ZipEntry entry = zipPatchFile().getEntry(name);
        if (null == entry)
            throw new InvalidZipPatchFileException(zipPatchFile().getName(),
                    new MissingZipEntryException(name));
        return entry;
    }

    /** A builder for a ZIP patch. */
    public static final class Builder {

        private ZipFile zipPatchFile, inputZipFile;
        private boolean outputJarFile;

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

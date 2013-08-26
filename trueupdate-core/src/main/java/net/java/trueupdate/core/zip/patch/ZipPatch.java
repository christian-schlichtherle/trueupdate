/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.core.zip.patch;

import edu.umd.cs.findbugs.annotations.CreatesObligation;
import java.io.*;
import java.security.*;
import java.util.zip.*;
import javax.annotation.*;
import javax.annotation.concurrent.NotThreadSafe;
import net.java.trueupdate.core.io.*;
import net.java.trueupdate.core.zip.model.*;
import static net.java.trueupdate.shed.Objects.requireNonNull;

/**
 * Applies a ZIP patch file to an input ZIP file and writes an output ZIP file.
 *
 * @author Christian Schlichtherle
 */
@NotThreadSafe
public abstract class ZipPatch {

    private DiffModel model;

    /** Returns the input ZIP file. */
    abstract @WillNotClose ZipFile inputZip();

    /** Returns the patch ZIP file. */
    abstract @WillNotClose ZipFile patchZip();

    /** Returns a new builder for a ZIP patch. */
    public static Builder builder() { return new Builder(); }

    /**
     * Applies the configured patch ZIP file.
     *
     * @param outputZip the sink for writing the output ZIP or JAR file.
     */
    public final void applyPatchZipTo(final Sink outputZip)
    throws IOException {
        final EntryNameFilter[] passFilters = passFilters();
        if (null == passFilters || 0 >= passFilters.length)
            throw new IllegalStateException("At least one pass filter is required to output anything.");

        class ApplyPatchZipTask implements ZipOutputTask<Void, IOException> {
            @Override public Void execute(final ZipOutputStream zipOut)
            throws IOException {
                for (EntryNameFilter filter : passFilters)
                    applyPatchFileTo(new NoDirectoryEntryNameFilter(filter), zipOut);
                return null;
            }
        }

        ZipSinks.execute(new ApplyPatchZipTask())
                .on(newZipOutputStream(outputZip));
    }

    /** Returns a new ZIP output stream which writes to the given sink. */
    @CreatesObligation ZipOutputStream newZipOutputStream(Sink outputFile)
    throws IOException {
        return new ZipOutputStream(outputFile.output());
    }

    /** Returns a new ZIP entry with the given name. */
    ZipEntry newEntry(String name) { return new ZipEntry(name); }

    /**
     * Returns a list of filters for the different passes required to process
     * the output ZIP file.
     * At least one filter is required to output anything.
     * The filters should properly partition the set of entry sources,
     * i.e. each entry source should be accepted by exactly one filter.
     */
    EntryNameFilter[] passFilters() {
        return new EntryNameFilter[] { new AcceptAllEntryNameFilter() };
    }

    private void applyPatchFileTo(
            final EntryNameFilter filter,
            final @WillNotClose ZipOutputStream zipOut)
    throws IOException {

        final MessageDigest digest = digest();

        class ZipEntrySink implements Sink {

            final EntryNameAndDigest zipEntryNameAndDigestValue;

            ZipEntrySink(final EntryNameAndDigest
                                 zipEntryNameAndDigestValue) {
                assert null != zipEntryNameAndDigestValue;
                this.zipEntryNameAndDigestValue = zipEntryNameAndDigestValue;
            }

            @Override public OutputStream output() throws IOException {
                final ZipEntry entry =
                        newEntry(zipEntryNameAndDigestValue.name());
                if (entry.isDirectory()) {
                    entry.setMethod(ZipOutputStream.STORED);
                    entry.setSize(0);
                    entry.setCompressedSize(0);
                    entry.setCrc(0);
                }
                zipOut.putNextEntry(entry);
                digest.reset();
                return new DigestOutputStream(zipOut, digest) {

                    @Override public void close() throws IOException {
                        ((ZipOutputStream) out).closeEntry();
                        if (!valueOfDigest().equals(
                                zipEntryNameAndDigestValue.digest()))
                            throw new WrongMessageDigestException(
                                    zipEntryNameAndDigestValue.name());
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
                    final Iterable<T> iterable)
            throws IOException {
                for (final T item : iterable) {
                    final EntryNameAndDigest
                            entryNameAndDigest = transformation.apply(item);
                    final String name = entryNameAndDigest.name();
                    if (!filter.accept(name)) continue;
                    final ZipEntry entry = source().getEntry(name);
                    if (null == entry)
                        throw ioException(new MissingZipEntryException(name));
                    try {
                        Copy.copy(
                                new ZipEntrySource(entry, source()),
                                new ZipEntrySink(entryNameAndDigest));
                    } catch (WrongMessageDigestException ex) {
                        throw ioException(ex);
                    }
                }
                return this;
            }
        } // PatchSet

        class InputFilePatchSet extends PatchSet {

            @Override ZipFile source() { return inputZip(); }

            @Override IOException ioException(Throwable cause) {
                return new WrongInputZipFile(source().getName(), cause);
            }
        } // InputFilePatchSet

        class PatchFilePatchSet extends PatchSet {

            @Override ZipFile source() { return patchZip(); }

            @Override IOException ioException(Throwable cause) {
                return new InvalidZipPatchFileException(source().getName(), cause);
            }
        } // PatchFilePatchSet

        // Order is important here!
        new InputFilePatchSet().apply(
                new IdentityTransformation(),
                diffModel().unchangedEntries());
        new PatchFilePatchSet().apply(
                new EntryNameAndTwoDigestsTransformation(),
                diffModel().changedEntries());
        new PatchFilePatchSet().apply(
                new IdentityTransformation(),
                diffModel().addedEntries());
    }

    private MessageDigest digest() throws IOException {
        return MessageDigests.create(diffModel().digestAlgorithmName());
    }

    private DiffModel diffModel() throws IOException {
        final DiffModel model = this.model;
        return null != model ? model : (this.model = loadDiffModel());
    }

    private DiffModel loadDiffModel() throws IOException {
        try {
            return DiffModel.decodeFromXml(
                    new ZipEntrySource(diffModelEntry(), patchZip()));
        } catch (RuntimeException ex) {
            throw ex;
        } catch (IOException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new InvalidZipPatchFileException(patchZip().getName(), ex);
        }
    }

    private ZipEntry diffModelEntry() throws IOException {
        final String name = DiffModel.ENTRY_NAME;
        final ZipEntry entry = patchZip().getEntry(name);
        if (null == entry)
            throw new InvalidZipPatchFileException(patchZip().getName(),
                    new MissingZipEntryException(name));
        return entry;
    }

    /** A builder for a ZIP patch. */
    public static final class Builder {

        private @CheckForNull ZipFile patchZip, inputZip;
        private boolean createJar;

        Builder() { }

        public Builder inputZip(final @Nullable ZipFile inputZip) {
            this.inputZip = inputZip;
            return this;
        }

        public Builder patchZip(final @Nullable ZipFile patchZip) {
            this.patchZip = patchZip;
            return this;
        }

        public Builder createJar(final boolean createJar) {
            this.createJar = createJar;
            return this;
        }

        public ZipPatch build() {
            return create(inputZip, patchZip, createJar);
        }

        private static ZipPatch create(
                final ZipFile inputZip,
                final ZipFile patchZip,
                final boolean createJar) {
            requireNonNull(inputZip);
            requireNonNull(patchZip);
            if (createJar) {
                return new JarPatch() {
                    @Override ZipFile inputZip() { return inputZip; }
                    @Override ZipFile patchZip() { return patchZip; }
                };
            } else {
                return new ZipPatch() {
                    @Override ZipFile inputZip() { return inputZip; }
                    @Override ZipFile patchZip() { return patchZip; }
                };
            }
        }
    } // Builder
}

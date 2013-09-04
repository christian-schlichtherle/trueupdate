/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.core.zip.patch;

import java.io.*;
import java.security.*;
import java.util.jar.JarEntry;
import java.util.zip.*;
import javax.annotation.*;
import javax.annotation.concurrent.ThreadSafe;
import net.java.trueupdate.core.io.*;
import net.java.trueupdate.core.zip.*;
import net.java.trueupdate.core.zip.model.*;

/**
 * Applies a ZIP patch file to an input ZIP file and writes an output ZIP file.
 * Archives may be ZIP, JAR, EAR or WAR files.
 * This class requires you to implement its {@link ZipFile} properties.
 *
 * @author Christian Schlichtherle
 */
@ThreadSafe
public abstract class RawZipPatch {

    private volatile DiffModel model;

    /** Returns the input archive. */
    protected abstract @WillNotClose ZipInput input();

    /** Returns the diff archive. */
    protected abstract @WillNotClose ZipInput diff();

    /**
     * Applies the configured diff archive.
     *
     * @param output the ZIP output for writing the output archive.
     */
    public void output(final ZipOutput output) throws IOException {
        ZipSinks.execute(new PatchTask()).on(output);
    }

    private class PatchTask implements ZipOutputTask<Void, IOException> {
        @Override public Void execute(final @WillNotClose ZipOutput output)
        throws IOException {
            for (EntryNameFilter filter : passFilters(output))
                output(output, new NoDirectoryEntryNameFilter(filter));
            return null;
        }
    }

    /**
     * Returns a list of filters for the different passes required to process
     * the output ZIP file.
     * At least one filter is required to output anything.
     * The filters should properly partition the set of entry sources,
     * i.e. each entry source should be accepted by exactly one filter.
     */
    private EntryNameFilter[] passFilters(final ZipOutput output) {
        if (output.entry("") instanceof JarEntry) {
            // The JarInputStream class assumes that the file entry
            // "META-INF/MANIFEST.MF" should either be the first or the second
            // entry (if preceded by the directory entry "META-INF/"), so we
            // need to process the ZIP patch file in two passes with a
            // corresponding filter to ensure this order.
            // Note that the directory entry "META-INF/" is always part of the
            // unchanged patch set because it's content is always empty.
            // Thus, by copying the unchanged entries before the changed
            // entries, the directory entry "META-INF/" will always appear
            // before the file entry "META-INF/MANIFEST.MF".
            final EntryNameFilter manifestFilter = new ManifestEntryNameFilter();
            return new EntryNameFilter[] {
                    manifestFilter,
                    new InverseEntryNameFilter(manifestFilter)
            };
        } else {
            return new EntryNameFilter[] { new AcceptAllEntryNameFilter() };
        }
    }

    private void output(
            final @WillNotClose ZipOutput output,
            final EntryNameFilter filter)
    throws IOException {

        final MessageDigest digest = digest();

        class ZipEntrySink implements Sink {

            final EntryNameAndDigest entryNameAndDigest;

            ZipEntrySink(final EntryNameAndDigest entryNameAndDigest) {
                assert null != entryNameAndDigest;
                this.entryNameAndDigest = entryNameAndDigest;
            }

            @Override public OutputStream output() throws IOException {
                final ZipEntry entry = output.entry(entryNameAndDigest.name());
                if (entry.isDirectory()) {
                    entry.setMethod(ZipOutputStream.STORED);
                    entry.setSize(0);
                    entry.setCompressedSize(0);
                    entry.setCrc(0);
                }
                digest.reset();
                return new DigestOutputStream(output.output(entry), digest) {

                    @Override public void close() throws IOException {
                        super.close();
                        if (!valueOfDigest().equals(
                                entryNameAndDigest.digest()))
                            throw new WrongMessageDigestException(
                                    entryNameAndDigest.name());
                    }

                    String valueOfDigest() {
                        return MessageDigests.valueOf(digest);
                    }
                };
            }
        } // ZipEntrySink

        abstract class PatchSet {

            abstract ZipInput archive();

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
                    final ZipEntry entry = archive().entry(name);
                    if (null == entry)
                        throw ioException(new MissingZipEntryException(name));
                    try {
                        Copy.copy(
                                new ZipEntrySource(entry, archive()),
                                new ZipEntrySink(entryNameAndDigest));
                    } catch (WrongMessageDigestException ex) {
                        throw ioException(ex);
                    }
                }
                return this;
            }
        } // PatchSet

        class InputArchivePatchSet extends PatchSet {

            @Override ZipInput archive() { return input(); }

            @Override IOException ioException(Throwable cause) {
                return new WrongInputZipFile(cause);
            }
        } // InputArchivePatchSet

        class PatchArchivePatchSet extends PatchSet {

            @Override ZipInput archive() { return diff(); }

            @Override IOException ioException(Throwable cause) {
                return new InvalidDiffZipFileException(cause);
            }
        } // PatchArchivePatchSet

        // Order is important here!
        new InputArchivePatchSet().apply(
                new IdentityTransformation(),
                model().unchangedEntries());
        new PatchArchivePatchSet().apply(
                new EntryNameAndDigest2Transformation(),
                model().changedEntries());
        new PatchArchivePatchSet().apply(
                new IdentityTransformation(),
                model().addedEntries());
    }

    private MessageDigest digest() throws IOException {
        return MessageDigests.create(model().digestAlgorithmName());
    }

    private DiffModel model() throws IOException {
        final DiffModel model = this.model;
        return null != model ? model : (this.model = loadModel());
    }

    private DiffModel loadModel() throws IOException {
        try {
            return DiffModel.decodeFromXml(
                    new ZipEntrySource(modelZipEntry(), diff()));
        } catch (RuntimeException ex) {
            throw ex;
        } catch (IOException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new InvalidDiffZipFileException(ex);
        }
    }

    private ZipEntry modelZipEntry() throws IOException {
        final String name = DiffModel.ENTRY_NAME;
        final ZipEntry entry = diff().entry(name);
        if (null == entry)
            throw new InvalidDiffZipFileException(
                    new MissingZipEntryException(name));
        return entry;
    }
}

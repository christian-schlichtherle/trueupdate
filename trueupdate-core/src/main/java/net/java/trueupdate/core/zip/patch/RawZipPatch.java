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
    protected abstract @WillNotClose ZipFile input();

    /** Returns the diff archive. */
    protected abstract @WillNotClose ZipFile diff();

    /**
     * Applies the configured diff archive.
     *
     * @param out the output stream for writing the output archive.
     */
    public void output(final @WillClose OutputStream out) throws IOException {
        final EntryNameFilter[] passFilters = passFilters();
        if (null == passFilters || 0 >= passFilters.length)
            throw new IllegalStateException("At least one pass filter is required to output anything.");

        class PatchTask implements ZipOutputTask<Void, IOException> {
            @Override public Void execute(final ZipOutputStream zipOut)
            throws IOException {
                for (EntryNameFilter filter : passFilters)
                    output(zipOut, new NoDirectoryEntryNameFilter(filter));
                return null;
            }
        }

        ZipSinks.execute(new PatchTask()).on(newZipOutputStream(out));
    }

    /** Returns a new ZIP output stream which writes to the given sink. */
    @CreatesObligation
    ZipOutputStream newZipOutputStream(OutputStream out) throws IOException {
        return new ZipOutputStream(out);
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
    EntryNameFilter[] passFilters() {
        return new EntryNameFilter[] { new AcceptAllEntryNameFilter() };
    }

    private void output(
            final @WillNotClose ZipOutputStream out,
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
                final ZipEntry entry = newZipEntry(entryNameAndDigest.name());
                if (entry.isDirectory()) {
                    entry.setMethod(ZipOutputStream.STORED);
                    entry.setSize(0);
                    entry.setCompressedSize(0);
                    entry.setCrc(0);
                }
                out.putNextEntry(entry);
                digest.reset();
                return new DigestOutputStream(out, digest) {

                    @Override public void close() throws IOException {
                        ((ZipOutputStream) out).closeEntry();
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

            abstract ZipFile archive();

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
                    final ZipEntry entry = archive().getEntry(name);
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

            @Override ZipFile archive() { return input(); }

            @Override IOException ioException(Throwable cause) {
                return new WrongInputZipFile(archive().getName(), cause);
            }
        } // InputArchivePatchSet

        class PatchArchivePatchSet extends PatchSet {

            @Override ZipFile archive() { return diff(); }

            @Override IOException ioException(Throwable cause) {
                return new InvalidZipPatchFileException(archive().getName(), cause);
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
            throw new InvalidZipPatchFileException(diff().getName(), ex);
        }
    }

    private ZipEntry modelZipEntry() throws IOException {
        final String name = DiffModel.ENTRY_NAME;
        final ZipEntry entry = diff().getEntry(name);
        if (null == entry)
            throw new InvalidZipPatchFileException(diff().getName(),
                    new MissingZipEntryException(name));
        return entry;
    }
}

/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.core.zip.diff;

import java.io.*;
import java.security.MessageDigest;
import java.util.*;
import static java.util.Objects.requireNonNull;
import java.util.zip.*;
import javax.annotation.*;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.NotThreadSafe;
import net.java.trueupdate.core.io.Copy;
import net.java.trueupdate.core.io.Sink;
import net.java.trueupdate.core.io.Source;
import net.java.trueupdate.core.util.MessageDigests;
import net.java.trueupdate.core.zip.util.EntrySource;
import net.java.trueupdate.core.zip.model.Diff;
import net.java.trueupdate.core.zip.model.EntryNameAndDigest;
import net.java.trueupdate.core.zip.model.EntryNameAndTwoDigests;

/**
 * Compares two ZIP files entry by entry.
 *
 * @author Christian Schlichtherle
 */
@NotThreadSafe
public abstract class ZipDiff {

    /** Returns the first ZIP file. */
    abstract @WillNotClose ZipFile firstZipFile();

    /** Returns the second ZIP file. */
    abstract @WillNotClose ZipFile secondZipFile();

    /** Returns the message digest. */
    abstract MessageDigest messageDigest();

    /**
     * Writes a ZIP patch file to the given sink.
     *
     * @param zipPatchFile the sink for writing the ZIP patch file.
     */
    public void writeZipPatchFileTo(final Sink zipPatchFile) throws IOException {
        final Diff diff = computeDiff();
        try (ZipOutputStream out = new ZipOutputStream(zipPatchFile.output())) {
            streamZipPatchFileTo(diff, out);
        }
    }

    private void streamZipPatchFileTo(
            final Diff diff,
            final @WillNotClose ZipOutputStream out)
    throws IOException {
        out.setLevel(Deflater.BEST_COMPRESSION);

        class EntrySink implements Sink {

            final String name;

            EntrySink(final String name) { this.name = name; }

            @Override public OutputStream output() throws IOException {
                out.putNextEntry(new ZipEntry(name));
                return new FilterOutputStream(out) {
                    @Override public void close() throws IOException {
                        ((ZipOutputStream) out).closeEntry();
                    }
                };
            }
        } // EntrySink

        class ZipPatchFileStreamer {

            final Diff diff;

            ZipPatchFileStreamer(final Diff diff) throws IOException {
                try {
                    diff.encodeToXml(new EntrySink(Diff.ENTRY_NAME));
                } catch (IOException | RuntimeException ex) {
                    throw ex;
                } catch (Exception ex) {
                    throw new IOException(ex);
                }
                this.diff = diff;
            }

            ZipPatchFileStreamer streamAddedOrChanged() throws IOException {
                for (final Enumeration<? extends ZipEntry>
                             entries = secondZipFile().entries();
                     entries.hasMoreElements(); ) {
                    final ZipEntry entry = entries.nextElement();
                    final String name = entry.getName();
                    if (addedOrChanged(name))
                        Copy.copy(new EntrySource(entry, secondZipFile()),
                                new EntrySink(name));
                }
                return this;
            }

            boolean addedOrChanged(String name) {
                return null != diff.added(name) ||
                        null != diff.changed(name);
            }
        } // ZipPatchFileStreamer

        new ZipPatchFileStreamer(diff).streamAddedOrChanged();
    }

    /** Computes a diff model from the two ZIP files. */
    public Diff computeDiff() throws IOException {
        return new Assembler().walkAndReturn(new Assembly()).buildDiff();
    }

    @Immutable
    private final class Assembler {

        /**
         * Walks the given visitor through the two ZIP files and returns it.
         * If and only if the visitor throws an I/O exception, the assembler
         * stops the visit and passes it on to the caller.
         */
        <V extends Visitor> V walkAndReturn(final V visitor)
        throws IOException {
            for (final Enumeration<? extends ZipEntry>
                         entries = firstZipFile().entries();
                 entries.hasMoreElements(); ) {
                final ZipEntry firstEntry = entries.nextElement();
                final ZipEntry secondEntry =
                        secondZipFile().getEntry(firstEntry.getName());
                final EntrySource firstEntrySource =
                        new EntrySource(firstEntry, firstZipFile());
                if (null == secondEntry)
                    visitor.visitEntryInFirstFile(firstEntrySource);
                else
                    visitor.visitEntriesInBothFiles(firstEntrySource,
                            new EntrySource(secondEntry, secondZipFile()));
            }

            for (final Enumeration<? extends ZipEntry>
                         entries = secondZipFile().entries();
                 entries.hasMoreElements(); ) {
                final ZipEntry secondEntry = entries.nextElement();
                final ZipEntry firstEntry =
                        firstZipFile().getEntry(secondEntry.getName());
                if (null == firstEntry)
                    visitor.visitEntryInSecondFile(
                            new EntrySource(secondEntry, secondZipFile()));
            }

            return visitor;
        }
    } // Assembler

    private class Assembly implements Visitor {

        private final Map<String, EntryNameAndTwoDigests>
                changed = new TreeMap<>();

        private final Map<String, EntryNameAndDigest>
                unchanged = new TreeMap<>(),
                added = new TreeMap<>(),
                removed = new TreeMap<>();

        Diff buildDiff() {
            return new Diff.Builder()
                    .digest(messageDigest())
                    .unchanged(unchanged.values())
                    .changed(changed.values())
                    .added(added.values())
                    .removed(removed.values())
                    .build();
        }

        @Override
        public void visitEntriesInBothFiles(EntrySource firstEntrySource,
                                            EntrySource secondEntrySource)
        throws IOException {
            final String firstName = firstEntrySource.name();
            assert firstName.equals(secondEntrySource.name());
            final String firstDigest = digestToHexString(firstEntrySource);
            final String secondDigest = digestToHexString(secondEntrySource);
            if (firstDigest.equals(secondDigest))
                unchanged.put(firstName, new EntryNameAndDigest(
                        firstName, firstDigest));
            else
                changed.put(firstName, new EntryNameAndTwoDigests(
                        firstName, firstDigest, secondDigest));
        }

        @Override
        public void visitEntryInFirstFile(EntrySource entrySource)
        throws IOException {
            final String name = entrySource.name();
            removed.put(name, new EntryNameAndDigest(name,
                    digestToHexString(entrySource)));
        }

        @Override
        public void visitEntryInSecondFile(EntrySource entrySource)
        throws IOException {
            final String name = entrySource.name();
            added.put(name, new EntryNameAndDigest(name,
                    digestToHexString(entrySource)));
        }

        String digestToHexString(Source source) throws IOException {
            return MessageDigests.digestToHexString(messageDigest(), source);
        }
    } // Assembly

    /**
     * A visitor of two ZIP files.
     * Note that the order of the calls to the visitor methods is undefined,
     * so you should not depend on the behavior of the current implementation
     * in order to ensure compatibility with future versions.
     */
    private interface Visitor {

        /**
         * Visits a ZIP entry which is present in the first ZIP file,
         * but not in the second ZIP file.
         *
         * @param entrySource1 the ZIP entry in the first ZIP file.
         */
        void visitEntryInFirstFile(EntrySource entrySource1)
        throws IOException;

        /**
         * Visits a ZIP entry which is present in the second ZIP file,
         * but not in the first ZIP file.
         *
         * @param entrySource2 the ZIP entry in the second ZIP file.
         */
        void visitEntryInSecondFile(EntrySource entrySource2)
        throws IOException;

        /**
         * Visits a pair of ZIP entries with equal names in the first and
         * second ZIP file.
         *
         * @param firstEntrySource the ZIP entry in the first ZIP file.
         * @param secondEntrySource the ZIP entry in the second ZIP file.
         */
        void visitEntriesInBothFiles(EntrySource firstEntrySource,
                                     EntrySource secondEntrySource)
        throws IOException;
    } // Visitor

    /**
     * A builder for a ZIP diff.
     * The default message digest is SHA-1 and the default JAXB context binds
     * only the {@link Diff} class.
     */
    public static final class Builder {

        private ZipFile firstZipFile, secondZipFile;
        private MessageDigest messageDigest;

        public Builder firstZipFile(final ZipFile firstZipFile) {
            this.firstZipFile = requireNonNull(firstZipFile);
            return this;
        }

        public Builder secondZipFile(final ZipFile secondZipFile) {
            this.secondZipFile = requireNonNull(secondZipFile);
            return this;
        }

        public Builder messageDigest(final MessageDigest messageDigest) {
            this.messageDigest = requireNonNull(messageDigest);
            return this;
        }

        public ZipDiff build() {
            return create(firstZipFile, secondZipFile,
                    null != messageDigest ? messageDigest : MessageDigests.sha1());
        }

        private static ZipDiff create(
                final ZipFile firstZipFile,
                final ZipFile secondZipFile,
                final MessageDigest messageDigest) {
            requireNonNull(firstZipFile);
            requireNonNull(secondZipFile);
            assert null != messageDigest;
            return new ZipDiff() {
                @Override ZipFile firstZipFile() { return firstZipFile; }
                @Override ZipFile secondZipFile() { return secondZipFile; }
                @Override MessageDigest messageDigest() { return messageDigest; }
            };
        }
    } // Builder
}

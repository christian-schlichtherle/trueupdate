/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.core.zip.diff;

import net.java.trueupdate.core.util.MessageDigests;
import net.java.trueupdate.core.zip.EntrySource;
import net.java.trueupdate.core.io.Copy;
import net.java.trueupdate.core.io.Sink;
import net.java.trueupdate.core.io.Source;
import net.java.trueupdate.core.zip.model.Diff;
import net.java.trueupdate.core.zip.model.EntryNameAndDigest;
import net.java.trueupdate.core.zip.model.EntryNameAndTwoDigests;

import java.io.*;
import java.security.MessageDigest;
import java.util.*;
import static java.util.Objects.requireNonNull;
import java.util.zip.*;
import javax.annotation.*;
import javax.annotation.concurrent.NotThreadSafe;

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
        final Computer computer = new Computer();
        new Engine().accept(computer);

        {
            final Diff diff = new Diff();
            diff.algorithm = algorithm();
            diff.numBytes = numBytes();
            diff.removed = nonEmptyOrNull(computer.removed);
            diff.added = nonEmptyOrNull(computer.added);
            diff.unchanged = nonEmptyOrNull(computer.unchanged);
            diff.changed = nonEmptyOrNull(computer.changed);
            return diff;
        }
    }

    private String algorithm() { return messageDigest().getAlgorithm(); }

    private Integer numBytes() {
        final MessageDigest digest = messageDigest();
        try {
            final MessageDigest
                    clone = MessageDigests.newDigest(digest.getAlgorithm());
            if (clone.getDigestLength() == digest.getDigestLength())
                return null;
        } catch (IllegalArgumentException fallThrough) {
        }
        return digest.getDigestLength();
    }

    private static @Nullable <X> SortedMap<String, X>
    nonEmptyOrNull(SortedMap<String, X> map) {
        return map.isEmpty() ? null : map;
    }

    private class Engine {

        <X extends Exception> void accept(final Visitor<X> visitor) throws X {
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
        }
    } // Engine

    private class Computer implements Visitor<IOException> {

        final SortedMap<String, EntryNameAndDigest>
                removed = new TreeMap<>(),
                added = new TreeMap<>(),
                unchanged = new TreeMap<>();

        final SortedMap<String, EntryNameAndTwoDigests>
                changed = new TreeMap<>();

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

        @Override
        public void visitEntriesInBothFiles(EntrySource firstEntrySource,
                                            EntrySource secondEntrySource)
        throws IOException {
            final String firstName = firstEntrySource.name();
            assert firstName.equals(secondEntrySource.name());
            final String firstDigest = digestToHexString(firstEntrySource);
            final String secondDigest = digestToHexString(secondEntrySource);
            if (firstDigest.equals(secondDigest))
                unchanged.put(firstName, new EntryNameAndDigest(firstName, firstDigest));
            else
                changed.put(firstName,
                        new EntryNameAndTwoDigests(firstName, firstDigest, secondDigest));
        }

        String digestToHexString(Source source) throws IOException {
            return MessageDigests.digestToHexString(messageDigest(), source);
        }
    } // Computer

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

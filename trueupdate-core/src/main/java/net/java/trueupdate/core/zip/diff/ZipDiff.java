/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.core.zip.diff;

import java.io.*;
import java.security.MessageDigest;
import java.util.*;
import java.util.zip.*;
import javax.annotation.*;
import javax.annotation.concurrent.*;
import net.java.trueupdate.core.io.*;
import net.java.trueupdate.core.zip.model.*;
import static net.java.trueupdate.shed.Objects.requireNonNull;

/**
 * Compares two archives entry by entry.
 * Archives may be ZIP, JAR, EAR or WAR files.
 *
 * @author Christian Schlichtherle
 */
@NotThreadSafe
public abstract class ZipDiff {

    /** Returns the first archive. */
    abstract @WillNotClose ZipFile archive1();

    /** Returns the second archive. */
    abstract @WillNotClose ZipFile archive2();

    /** Returns the message digest. */
    abstract MessageDigest digest();

    /** Returns a new builder for a ZIP diff. */
    public static Builder builder() { return new Builder(); }

    /**
     * Writes a patch archive to the given sink.
     *
     * @param patchArchive the sink for writing the patch archive.
     */
    public void writePatchArchiveTo(final Sink patchArchive) throws IOException {
        final DiffModel model = computeDiffModel();

        class StreamPatchArchiveTask implements ZipOutputTask<Void, IOException> {
            @Override public Void execute(final ZipOutputStream zipOut)
            throws IOException {
                streamPatchArchiveTo(model, zipOut);
                return null;
            }
        }

        ZipSinks.execute(new StreamPatchArchiveTask())
                .on(new ZipOutputStream(patchArchive.output()));
    }

    private void streamPatchArchiveTo(
            final DiffModel model,
            final @WillNotClose ZipOutputStream zipOut)
    throws IOException {
        zipOut.setLevel(Deflater.BEST_COMPRESSION);

        final class PatchArchiveStreamer {

            final DiffModel model;

            PatchArchiveStreamer(final DiffModel model) throws IOException {
                try {
                    model.encodeToXml(entrySink(DiffModel.ENTRY_NAME));
                } catch (RuntimeException ex) {
                    throw ex;
                } catch (IOException ex) {
                    throw ex;
                } catch (Exception ex) {
                    throw new IOException(ex);
                }
                this.model = model;
            }

            PatchArchiveStreamer streamChangedOrAdded() throws IOException {
                for (final Enumeration<? extends ZipEntry>
                             entries = archive2().entries();
                     entries.hasMoreElements(); ) {
                    final ZipEntry entry = entries.nextElement();
                    final String name = entry.getName();
                    if (changedOrAdded(name))
                        Copy.copy(new ZipEntrySource(entry, archive2()),
                                  entrySink(name));
                }
                return this;
            }

            Sink entrySink(String name) {
                return new ZipEntrySink(new ZipEntry(name), zipOut);
            }

            boolean changedOrAdded(String name) {
                return null != model.changed(name) || null != model.added(name);
            }
        } // PatchArchiveStreamer

        new PatchArchiveStreamer(model).streamChangedOrAdded();
    }

    /** Computes a diff model from the two configured archives. */
    public DiffModel computeDiffModel() throws IOException {
        return new Assembler().walkAndReturn(new Assembly()).buildZipDiffModel();
    }

    @Immutable
    private class Assembler {

        /**
         * Walks the given visitor through the two ZIP files and returns it.
         * If and only if the visitor throws an I/O exception, the assembler
         * stops the visit and passes it on to the caller.
         */
        <V extends Visitor> V walkAndReturn(final V visitor)
        throws IOException {
            for (final Enumeration<? extends ZipEntry>
                         entries = archive1().entries();
                 entries.hasMoreElements(); ) {
                final ZipEntry entry1 = entries.nextElement();
                final String name = entry1.getName();
                if (name.endsWith("/")) continue;
                final ZipEntry entry2 = archive2().getEntry(name);
                final ZipEntrySource source1 =
                        new ZipEntrySource(entry1, archive1());
                if (null == entry2)
                    visitor.visitEntryInFirstFile(source1);
                else
                    visitor.visitEntriesInBothFiles(source1,
                            new ZipEntrySource(entry2, archive2()));
            }

            for (final Enumeration<? extends ZipEntry>
                         entries = archive2().entries();
                 entries.hasMoreElements(); ) {
                final ZipEntry entry2 = entries.nextElement();
                final String name = entry2.getName();
                if (name.endsWith("/")) continue;
                final ZipEntry entry1 = archive1().getEntry(name);
                if (null == entry1)
                    visitor.visitEntryInSecondFile(
                            new ZipEntrySource(entry2, archive2()));
            }

            return visitor;
        }
    } // Assembler

    private class Assembly implements Visitor {

        private final Map<String, EntryNameAndTwoDigests>
                changed = new TreeMap<String, EntryNameAndTwoDigests>();

        private final Map<String, EntryNameAndDigest>
                unchanged = new TreeMap<String, EntryNameAndDigest>(),
                added = new TreeMap<String, EntryNameAndDigest>(),
                removed = new TreeMap<String, EntryNameAndDigest>();

        DiffModel buildZipDiffModel() {
            return DiffModel
                    .builder()
                    .messageDigest(digest())
                    .changedEntries(changed.values())
                    .unchangedEntries(unchanged.values())
                    .addedEntries(added.values())
                    .removedEntries(removed.values())
                    .build();
        }

        @Override
        public void visitEntriesInBothFiles(
                final ZipEntrySource source1,
                final ZipEntrySource source2)
        throws IOException {
            final String name1 = source1.name();
            assert name1.equals(source2.name());
            final String digest1 = digestValueOf(source1);
            final String digest2 = digestValueOf(source2);
            if (digest1.equals(digest2))
                unchanged.put(name1,
                        new EntryNameAndDigest(name1, digest1));
            else
                changed.put(name1,
                        new EntryNameAndTwoDigests(name1, digest1, digest2));
        }

        @Override
        public void visitEntryInFirstFile(final ZipEntrySource source1)
        throws IOException {
            final String name = source1.name();
            removed.put(name, new EntryNameAndDigest(name,
                    digestValueOf(source1)));
        }

        @Override
        public void visitEntryInSecondFile(final ZipEntrySource source2)
        throws IOException {
            final String name = source2.name();
            added.put(name, new EntryNameAndDigest(name,
                    digestValueOf(source2)));
        }

        String digestValueOf(Source source) throws IOException {
            final MessageDigest messageDigest = digest();
            messageDigest.reset();
            MessageDigests.updateDigestFrom(messageDigest, source);
            return MessageDigests.valueOf(messageDigest);
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
         * @param source1 the ZIP entry in the first ZIP file.
         */
        void visitEntryInFirstFile(ZipEntrySource source1)
        throws IOException;

        /**
         * Visits a ZIP entry which is present in the second ZIP file,
         * but not in the first ZIP file.
         *
         * @param source2 the ZIP entry in the second ZIP file.
         */
        void visitEntryInSecondFile(ZipEntrySource source2)
        throws IOException;

        /**
         * Visits a pair of ZIP entries with equal names in the first and
         * second ZIP file.
         *
         * @param source1 the ZIP entry in the first ZIP file.
         * @param source2 the ZIP entry in the second ZIP file.
         */
        void visitEntriesInBothFiles(ZipEntrySource source1,
                                     ZipEntrySource source2)
        throws IOException;
    } // Visitor

    /**
     * A builder for a ZIP diff.
     * The default message digest is SHA-1.
     */
    public static class Builder {

        private @CheckForNull ZipFile archive1, archive2;
        private @CheckForNull MessageDigest digest;

        Builder() { }

        public Builder archive1(final @Nullable ZipFile archive2) {
            this.archive1 = archive2;
            return this;
        }

        public Builder archive2(final @Nullable ZipFile archive2) {
            this.archive2 = archive2;
            return this;
        }

        public Builder digest(final @Nullable MessageDigest digest) {
            this.digest = digest;
            return this;
        }

        public ZipDiff build() {
            return create(archive1, archive2, nonNullOrSha1(digest));
        }

        private static MessageDigest nonNullOrSha1(
                final @CheckForNull MessageDigest digest) {
            return null != digest ? digest : MessageDigests.sha1();
        }

        private static ZipDiff create(
                final ZipFile archive1,
                final ZipFile archive2,
                final MessageDigest digest) {
            requireNonNull(archive1);
            requireNonNull(archive2);
            assert null != digest;
            return new ZipDiff() {
                @Override ZipFile archive1() { return archive1; }
                @Override ZipFile archive2() { return archive2; }
                @Override MessageDigest digest() { return digest; }
            };
        }
    } // Builder
}

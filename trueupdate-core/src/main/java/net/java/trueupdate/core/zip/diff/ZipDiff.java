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
import javax.annotation.concurrent.*;
import net.java.trueupdate.core.io.*;
import net.java.trueupdate.core.util.*;
import net.java.trueupdate.core.zip.model.*;

/**
 * Compares two ZIP files entry by entry.
 *
 * @author Christian Schlichtherle
 */
@NotThreadSafe
public abstract class ZipDiff {

    /** Returns the first ZIP file. */
    abstract @WillNotClose ZipFile zipFile1();

    /** Returns the second ZIP file. */
    abstract @WillNotClose ZipFile zipFile2();

    /** Returns the message digest. */
    abstract MessageDigest messageDigest();

    /** Returns a new builder for a ZIP diff. */
    public static Builder builder() { return new Builder(); }

    /**
     * Writes a ZIP patch file to the given sink.
     *
     * @param zipPatchFile the sink for writing the ZIP patch file.
     */
    public void writeZipPatchFileTo(final Sink zipPatchFile) throws IOException {
        final ZipDiffModel model = computeZipDiffModel();
        try (ZipOutputStream out = new ZipOutputStream(zipPatchFile.output())) {
            streamZipPatchFileTo(model, out);
        }
    }

    private void streamZipPatchFileTo(
            final ZipDiffModel model,
            final @WillNotClose ZipOutputStream out)
    throws IOException {
        out.setLevel(Deflater.BEST_COMPRESSION);

        final class ZipPatchFileStreamer {

            final ZipDiffModel model;

            ZipPatchFileStreamer(final ZipDiffModel model) throws IOException {
                try {
                    model.encodeToXml(entrySink(ZipDiffModel.ENTRY_NAME));
                } catch (IOException | RuntimeException ex) {
                    throw ex;
                } catch (Exception ex) {
                    throw new IOException(ex);
                }
                this.model = model;
            }

            ZipPatchFileStreamer streamChangedOrAdded() throws IOException {
                for (final Enumeration<? extends ZipEntry>
                             entries = zipFile2().entries();
                     entries.hasMoreElements(); ) {
                    final ZipEntry entry = entries.nextElement();
                    final String name = entry.getName();
                    if (changedOrAdded(name))
                        Copy.copy(new ZipEntrySource(entry, zipFile2()),
                                  entrySink(name));
                }
                return this;
            }

            Sink entrySink(String name) {
                return new ZipEntrySink(new ZipEntry(name), out);
            }

            boolean changedOrAdded(String name) {
                return null != model.changed(name) || null != model.added(name);
            }
        } // ZipPatchFileStreamer

        new ZipPatchFileStreamer(model).streamChangedOrAdded();
    }

    /** Computes a ZIP diff model from the two ZIP files. */
    public ZipDiffModel computeZipDiffModel() throws IOException {
        return new Assembler().walkAndReturn(new Assembly()).buildZipDiffModel();
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
                         entries = zipFile1().entries();
                 entries.hasMoreElements(); ) {
                final ZipEntry zipEntry1 = entries.nextElement();
                final ZipEntry zipEntry2 =
                        zipFile2().getEntry(zipEntry1.getName());
                final ZipEntrySource zipEntrySource1 =
                        new ZipEntrySource(zipEntry1, zipFile1());
                if (null == zipEntry2)
                    visitor.visitEntryInFirstFile(zipEntrySource1);
                else
                    visitor.visitEntriesInBothFiles(zipEntrySource1,
                            new ZipEntrySource(zipEntry2, zipFile2()));
            }

            for (final Enumeration<? extends ZipEntry>
                         entries = zipFile2().entries();
                 entries.hasMoreElements(); ) {
                final ZipEntry zipEntry2 = entries.nextElement();
                final ZipEntry zipEntry1 =
                        zipFile1().getEntry(zipEntry2.getName());
                if (null == zipEntry1)
                    visitor.visitEntryInSecondFile(
                            new ZipEntrySource(zipEntry2, zipFile2()));
            }

            return visitor;
        }
    } // Assembler

    private class Assembly implements Visitor {

        private final Map<String, ZipEntryNameAndTwoDigestValues>
                changed = new TreeMap<>();

        private final Map<String, ZipEntryNameAndDigestValue>
                unchanged = new TreeMap<>(),
                added = new TreeMap<>(),
                removed = new TreeMap<>();

        ZipDiffModel buildZipDiffModel() {
            return ZipDiffModel
                    .builder()
                    .messageDigest(messageDigest())
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
            final String value1 = digestValueOf(source1);
            final String value2 = digestValueOf(source2);
            if (value1.equals(value2))
                unchanged.put(name1, new ZipEntryNameAndDigestValue(
                        name1, value1));
            else
                changed.put(name1, new ZipEntryNameAndTwoDigestValues(
                        name1, value1, value2));
        }

        @Override
        public void visitEntryInFirstFile(final ZipEntrySource source1)
        throws IOException {
            final String name = source1.name();
            removed.put(name, new ZipEntryNameAndDigestValue(name,
                    digestValueOf(source1)));
        }

        @Override
        public void visitEntryInSecondFile(final ZipEntrySource source2)
        throws IOException {
            final String name = source2.name();
            added.put(name, new ZipEntryNameAndDigestValue(name,
                    digestValueOf(source2)));
        }

        String digestValueOf(Source source) throws IOException {
            final MessageDigest messageDigest = messageDigest();
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
    public static final class Builder {

        private @CheckForNull ZipFile zipFile1, zipFile2;
        private @CheckForNull MessageDigest messageDigest;

        Builder() { }

        public Builder zipFile1(final @Nullable ZipFile zipFile1) {
            this.zipFile1 = zipFile1;
            return this;
        }

        public Builder zipFile2(final @Nullable ZipFile zipFile2) {
            this.zipFile2 = zipFile2;
            return this;
        }

        public Builder messageDigest(
                final @Nullable MessageDigest messageDigest) {
            this.messageDigest = messageDigest;
            return this;
        }

        public ZipDiff build() {
            return create(zipFile1, zipFile2, nonNullOrSha1(messageDigest));
        }

        private static MessageDigest nonNullOrSha1(
                final @CheckForNull MessageDigest messageDigest) {
            return null != messageDigest ? messageDigest : MessageDigests.sha1();
        }

        private static ZipDiff create(
                final ZipFile zipFile1,
                final ZipFile zipFile2,
                final MessageDigest messageDigest) {
            requireNonNull(zipFile1);
            requireNonNull(zipFile2);
            assert null != messageDigest;
            return new ZipDiff() {
                @Override ZipFile zipFile1() { return zipFile1; }
                @Override ZipFile zipFile2() { return zipFile2; }
                @Override MessageDigest messageDigest() { return messageDigest; }
            };
        }
    } // Builder
}

/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.core.zip.diff;

import java.io.*;
import java.security.MessageDigest;
import java.util.*;
import java.util.regex.Pattern;
import java.util.zip.*;
import javax.annotation.*;
import javax.annotation.concurrent.*;
import net.java.trueupdate.core.io.*;
import net.java.trueupdate.core.zip.*;
import net.java.trueupdate.core.zip.model.*;

/**
 * Compares two archives entry by entry.
 * Archives may be ZIP, JAR, EAR or WAR files.
 * This class requires you to implement its {@link ZipFile} and
 * {@link MessageDigest} properties, but enables you to obtain the ZIP diff
 * {@linkplain #model} besides {@linkplain #output}ting the diff archive.
 *
 * @author Christian Schlichtherle
 */
@Immutable
public abstract class RawZipDiff {

    private static final Pattern COMPRESSED_FILE_EXTENSIONS = Pattern.compile(
            ".*\\.(ear|jar|war|zip|gz|xz)", Pattern.CASE_INSENSITIVE);

    /** Returns the first archive. */
    protected abstract @WillNotClose ZipInput input1();

    /** Returns the second archive. */
    protected abstract @WillNotClose ZipInput input2();

    /** Returns the message digest. */
    protected abstract MessageDigest digest();

    /**
     * Writes a diff archive to the given ZIP output.
     *
     * @param output the ZIP output for writing the diff archive.
     */
    public void output(@WillClose ZipOutput output) throws IOException {
        ZipSinks.execute(new DiffTask()).on(output);
    }

    private class DiffTask implements ZipOutputTask<Void, IOException> {
        @Override public Void execute(final @WillNotClose ZipOutput output)
        throws IOException {
            output(output, model());
            return null;
        }
    }

    private void output(
            final @WillNotClose ZipOutput output,
            final DiffModel model)
    throws IOException {

        final class DiffStreamer {

            DiffStreamer() throws IOException {
                try {
                    model.encodeToXml(sink(output.entry(DiffModel.ENTRY_NAME)));
                } catch (RuntimeException ex) {
                    throw ex;
                } catch (IOException ex) {
                    throw ex;
                } catch (Exception ex) {
                    throw new IOException(ex);
                }
            }

            DiffStreamer stream() throws IOException {
                for (final ZipEntry in : input2()) {
                    final String name = in.getName();
                    if (changedOrAdded(name)) {
                        final ZipEntry out = output.entry(name);
                        if (COMPRESSED_FILE_EXTENSIONS.matcher(name).matches()) {
                            final long size = in.getSize();
                            out.setMethod(ZipOutputStream.STORED);
                            out.setSize(size);
                            out.setCompressedSize(size);
                            out.setCrc(in.getCrc());
                        }
                        Copy.copy(source2(in), sink(out));
                    }
                }
                return this;
            }

            Source source2(ZipEntry entry){
                return new ZipEntrySource(entry, input2());
            }

            Sink sink(ZipEntry entry) { return new ZipEntrySink(entry, output); }

            boolean changedOrAdded(String name) {
                return null != model.changed(name) || null != model.added(name);
            }
        } // DiffStreamer

        new DiffStreamer().stream();
    }

    /** Computes a diff model from the two configured archives. */
    public DiffModel model() throws IOException {
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
            for (final ZipEntry entry1 : input1()) {
                if (entry1.isDirectory()) continue;
                final ZipEntry entry2 = input2().entry(entry1.getName());
                final ZipEntrySource source1 =
                        new ZipEntrySource(entry1, input1());
                if (null == entry2)
                    visitor.visitEntryInFirstFile(source1);
                else
                    visitor.visitEntriesInBothFiles(source1,
                            new ZipEntrySource(entry2, input2()));
            }

            for (final ZipEntry entry2 : input2()) {
                if (entry2.isDirectory()) continue;
                final ZipEntry entry1 = input1().entry(entry2.getName());
                if (null == entry1)
                    visitor.visitEntryInSecondFile(
                            new ZipEntrySource(entry2, input2()));
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
            final MessageDigest digest = digest();
            digest.reset();
            MessageDigests.updateDigestFrom(digest, source);
            return MessageDigests.valueOf(digest);
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
}

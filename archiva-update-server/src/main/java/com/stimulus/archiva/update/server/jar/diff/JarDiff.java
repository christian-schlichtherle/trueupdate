/*
 * Copyright (C) 2005-2013 Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.server.jar.diff;

import com.stimulus.archiva.update.core.codec.JaxbCodec;
import com.stimulus.archiva.update.core.io.*;
import com.stimulus.archiva.update.server.jar.commons.EntrySource;
import com.stimulus.archiva.update.server.jar.model.*;
import com.stimulus.archiva.update.server.util.MessageDigests;
import java.io.*;
import java.security.MessageDigest;
import java.util.*;
import static java.util.Objects.requireNonNull;
import java.util.jar.*;
import java.util.zip.*;
import javax.annotation.*;
import javax.annotation.concurrent.NotThreadSafe;
import javax.xml.bind.*;

/**
 * Compares two JAR files entry by entry.
 *
 * @author Christian Schlichtherle
 */
@NotThreadSafe
public abstract class JarDiff {

    /** Returns the first JAR file. */
    abstract @WillNotClose JarFile firstJarFile();

    /** Returns the second JAR file. */
    abstract @WillNotClose JarFile secondJarFile();

    /** Returns the message digest. */
    abstract MessageDigest messageDigest();

    /** Returns the JAXB context for marshalling the JAR diff bean. */
    abstract JAXBContext jaxbContext();

    /**
     * Writes a JAR diff file to the given sink.
     *
     * @param jarPatchFile the sink for writing the JAR patch file.
     */
    public void writeDiffFileTo(final Sink jarPatchFile) throws IOException {
        final Diff diff = computeDiff();
        try (ZipOutputStream out = new ZipOutputStream(jarPatchFile.output())) {
            writeDiffFileTo(diff, out);
        }
    }

    private void writeDiffFileTo(
            final Diff diff,
            final @WillNotClose ZipOutputStream out)
    throws IOException {
        out.setLevel(Deflater.BEST_COMPRESSION);

        final class EntrySink implements Sink {

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

        final class JarPatchFileWriter {

            final Diff diff;

            JarPatchFileWriter(final Diff diff) throws IOException {
                try {
                    new JaxbCodec(jaxbContext()).encode(
                            new EntrySink(Diffs.DIFF_ENTRY_NAME), diff);
                } catch (IOException | RuntimeException ex) {
                    throw ex;
                } catch (Exception ex) {
                    throw new IOException(ex);
                }
                this.diff = diff;
            }

            JarPatchFileWriter writeAddedOrChanged() throws IOException {
                for (final Enumeration<JarEntry> entries = secondJarFile().entries();
                     entries.hasMoreElements(); ) {
                    final JarEntry entry = entries.nextElement();
                    final String name = entry.getName();
                    if (addedOrChanged(name))
                        Copy.copy(new EntrySource(entry, secondJarFile()),
                                new EntrySink(name));
                }
                return this;
            }

            boolean addedOrChanged(String name) {
                return null != diff.added(name) ||
                        null != diff.changed(name);
            }
        } // JarPatchFileWriter

        new JarPatchFileWriter(diff).writeAddedOrChanged();
    }

    /** Computes a JAR diff bean from the two JAR files. */
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
            for (final Enumeration<JarEntry> entries = firstJarFile().entries();
                 entries.hasMoreElements(); ) {
                final JarEntry firstEntry = entries.nextElement();
                final JarEntry secondEntry =
                        secondJarFile().getJarEntry(firstEntry.getName());
                final EntrySource firstEntrySource =
                        new EntrySource(firstEntry, firstJarFile());
                if (null == secondEntry)
                    visitor.visitEntryInFirstFile(firstEntrySource);
                else
                    visitor.visitEntriesInBothFiles(firstEntrySource,
                            new EntrySource(secondEntry, secondJarFile()));
            }

            for (final Enumeration<JarEntry> entries = secondJarFile().entries();
                 entries.hasMoreElements(); ) {
                final JarEntry secondEntry = entries.nextElement();
                final JarEntry firstEntry =
                        firstJarFile().getJarEntry(secondEntry.getName());
                if (null == firstEntry)
                    visitor.visitEntryInSecondFile(
                            new EntrySource(secondEntry, secondJarFile()));
            }
        }
    } // Engine

    private class Computer implements Visitor<IOException> {

        final SortedMap<String, EntryNameWithDigest>
                removed = new TreeMap<>(),
                added = new TreeMap<>(),
                unchanged = new TreeMap<>();

        final SortedMap<String, EntryNameWithTwoDigests>
                changed = new TreeMap<>();

        @Override
        public void visitEntryInFirstFile(EntrySource entrySource)
        throws IOException {
            final String name = entrySource.name();
            removed.put(name, new EntryNameWithDigest(name,
                    digestToHexString(entrySource)));
        }

        @Override
        public void visitEntryInSecondFile(EntrySource entrySource)
        throws IOException {
            final String name = entrySource.name();
            added.put(name, new EntryNameWithDigest(name,
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
                unchanged.put(firstName, new EntryNameWithDigest(firstName, firstDigest));
            else
                changed.put(firstName,
                        new EntryNameWithTwoDigests(firstName, firstDigest, secondDigest));
        }

        String digestToHexString(Source source) throws IOException {
            return MessageDigests.digestToHexString(messageDigest(), source);
        }
    } // Computer

    /**
     * A builder for a JAR diff.
     * The default message digest is SHA-1 and the default JAXB context binds
     * only the {@link Diff} class.
     */
    public static class Builder {

        private JarFile firstJarFile, secondJarFile;
        private MessageDigest messageDigest;
        private JAXBContext jaxbContext;

        public Builder firstJarFile(final JarFile firstJarFile) {
            this.firstJarFile = requireNonNull(firstJarFile);
            return this;
        }

        public Builder secondJarFile(final JarFile secondJarFile) {
            this.secondJarFile = requireNonNull(secondJarFile);
            return this;
        }

        public Builder messageDigest(final MessageDigest messageDigest) {
            this.messageDigest = requireNonNull(messageDigest);
            return this;
        }

        @Deprecated
        public Builder jaxbContext(final JAXBContext jaxbContext) {
            this.jaxbContext = requireNonNull(jaxbContext);
            return this;
        }

        public JarDiff build() {
            return build(firstJarFile, secondJarFile,
                    null != messageDigest ? messageDigest : MessageDigests.sha1(),
                    null != jaxbContext ? jaxbContext : Diffs.jaxbContext());
        }

        private static JarDiff build(
                final JarFile firstJarFile,
                final JarFile secondJarFile,
                final MessageDigest messageDigest,
                final JAXBContext jaxbContext) {
            requireNonNull(firstJarFile);
            requireNonNull(secondJarFile);
            assert null != messageDigest;
            assert null != jaxbContext;
            return new JarDiff() {
                @Override JarFile firstJarFile() { return firstJarFile; }
                @Override JarFile secondJarFile() { return secondJarFile; }
                @Override MessageDigest messageDigest() { return messageDigest; }
                @Override JAXBContext jaxbContext() { return jaxbContext; }
            };
        }
    } // Builder
}

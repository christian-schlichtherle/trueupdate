/*
 * Copyright (C) 2005-2013 Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.server.jar.diff;

import com.stimulus.archiva.update.core.codec.JaxbCodec;
import com.stimulus.archiva.update.core.io.*;
import com.stimulus.archiva.update.server.jar.model.*;
import com.stimulus.archiva.update.server.util.MessageDigests;
import java.io.*;
import java.security.MessageDigest;
import java.util.*;
import static java.util.Objects.requireNonNull;
import java.util.jar.*;
import java.util.zip.*;
import javax.annotation.*;
import javax.annotation.concurrent.Immutable;
import javax.xml.bind.*;

/**
 * Compares two JAR files entry by entry.
 *
 * @author Christian Schlichtherle
 */
@Immutable
public abstract class JarDiff {

    private static final JAXBContext JAXB_CONTEXT;

    static {
        try {
            JAXB_CONTEXT = JAXBContext.newInstance(Diff.class);
        } catch (JAXBException ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    /** Returns the first JAR file. */
    abstract JarFile jarFile1();

    /** Returns the second JAR file. */
    abstract JarFile jarFile2();

    /** Returns the message digest. */
    abstract MessageDigest messageDigest();

    /** Writes a JAR patch file to the given sink. */
    public void writePatchFileTo(final Sink sink) throws IOException {
        final Diff diff = computeDiff();
        try (ZipOutputStream out = new ZipOutputStream(sink.output())) {
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

            class WithZipOutputStream {
                WithZipOutputStream writeDiff() throws IOException {
                    try {
                        new JaxbCodec(JAXB_CONTEXT)
                                .encode(new EntrySink("diff"), diff);
                    } catch (IOException ex) {
                        throw ex;
                    } catch (Exception ex) {
                        throw new IOException(ex);
                    }
                    return this;
                }

                WithZipOutputStream writeAddedOrChanged() throws IOException {
                    final JarFile jarFile2 = jarFile2();
                    for (final Enumeration<JarEntry> e2 = jarFile2.entries();
                         e2.hasMoreElements(); ) {
                        final JarEntry entry2 = e2.nextElement();
                        Copy.copy(new EntrySource(entry2, jarFile2),
                                  new EntrySink(entry2.getName()));
                    }
                    return this;
                }
            } // WithZipOutputStream

            new WithZipOutputStream().writeDiff().writeAddedOrChanged();
        }
    }

    /** Computes a JAR diff from the two JAR files. */
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
            for (final Enumeration<JarEntry> e1 = jarFile1().entries();
                 e1.hasMoreElements(); ) {
                final JarEntry entry1 = e1.nextElement();
                final JarEntry entry2 = jarFile2().getJarEntry(entry1.getName());
                final EntrySource entrySource1 = new EntrySource(entry1, jarFile1());
                if (null == entry2)
                    visitor.visitEntryInFile1(entrySource1);
                else
                    visitor.visitEntriesInFiles(entrySource1,
                            new EntrySource(entry2, jarFile2()));
            }

            for (final Enumeration<JarEntry> e2 = jarFile2().entries();
                 e2.hasMoreElements(); ) {
                final JarEntry entry2 = e2.nextElement();
                final JarEntry entry1 = jarFile1().getJarEntry(entry2.getName());
                if (null == entry1)
                    visitor.visitEntryInFile2(new EntrySource(entry2, jarFile2()));
            }
        }
    } // Engine

    private class Computer implements Visitor<IOException> {
        final SortedMap<String, EntryDigest>
                removed = new TreeMap<>(),
                added = new TreeMap<>(),
                unchanged = new TreeMap<>();

        final SortedMap<String, BeforeAndAfterEntryDigest>
                changed = new TreeMap<>();

        @Override
        public void visitEntryInFile1(EntrySource entrySource1)
        throws IOException {
            final String name1 = entrySource1.name();
            removed.put(name1, new EntryDigest(name1,
                    digestToHexString(entrySource1)));
        }

        @Override
        public void visitEntryInFile2(EntrySource entrySource2)
        throws IOException {
            final String name2 = entrySource2.name();
            added.put(name2, new EntryDigest(name2,
                    digestToHexString(entrySource2)));
        }

        @Override
        public void visitEntriesInFiles(EntrySource entrySource1,
                                        EntrySource entrySource2)
        throws IOException {
            final String name1 = entrySource1.name();
            assert name1.equals(entrySource2.name());
            final String digest1 = digestToHexString(entrySource1);
            final String digest2 = digestToHexString(entrySource2);
            if (digest1.equals(digest2))
                unchanged.put(name1, new EntryDigest(name1, digest1));
            else
                changed.put(name1,
                        new BeforeAndAfterEntryDigest(name1, digest1, digest2));
        }

        String digestToHexString(Source source) throws IOException {
            return MessageDigests.digestToHexString(messageDigest(), source);
        }
    } // Computer

    /**
     * A builder for a JAR diff.
     * The default message digest is SHA-1.
     */
    public static class Builder {
        private JarFile file1, file2;
        private MessageDigest digest = MessageDigests.sha1();

        public Builder jarFile1(final JarFile jarFile1) {
            this.file1 = requireNonNull(jarFile1);
            return this;
        }

        public Builder jarFile2(final JarFile jarFile2) {
            this.file2 = requireNonNull(jarFile2);
            return this;
        }

        public Builder messageDigest(final MessageDigest messageDigest) {
            this.digest = requireNonNull(messageDigest);
            return this;
        }

        public JarDiff build() {
            return build(file1, file2, digest);
        }

        private static JarDiff build(
                final JarFile file1,
                final JarFile file2,
                final MessageDigest digest) {
            requireNonNull(file1);
            requireNonNull(file2);
            requireNonNull(digest);
            return new JarDiff() {
                @Override JarFile jarFile1() { return file1; }
                @Override JarFile jarFile2() { return file2; }
                @Override MessageDigest messageDigest() { return digest; }
            };
        }
    } // Builder
}

/*
 * Copyright (C) 2005-2013 Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.server.jar.diff;

import com.stimulus.archiva.update.core.io.*;
import com.stimulus.archiva.update.server.jar.model.*;
import com.stimulus.archiva.update.server.util.MessageDigests;
import java.io.*;
import java.security.MessageDigest;
import java.util.*;
import static java.util.Objects.requireNonNull;
import java.util.jar.JarFile;
import java.util.zip.*;
import javax.annotation.*;
import javax.annotation.concurrent.Immutable;

/**
 * Computes two JAR files entry by entry.
 *
 * @author Christian Schlichtherle
 */
@Immutable
public abstract class JarDiff {

    /** Returns the first JAR file. */
    public abstract JarFile jarFile1();

    /** Returns the second JAR file. */
    public abstract JarFile jarFile2();

    /** Returns the sink for writing the JAR patch file. */
    public abstract Sink patchFileSink();

    /** Returns the message digest. */
    public abstract MessageDigest messageDigest();

    /** Writes the JAR patch file. */
    public void writePatchFile() throws IOException {
        final Diff diff = computeDiff();
        try (ZipOutputStream out = new ZipOutputStream(patchFileSink().output())) {
            out.setLevel(Deflater.BEST_COMPRESSION);
            out.putNextEntry(new ZipEntry("diff"));

        }
    }

    private static void serializeTo(final Diff diff, final @WillNotClose OutputStream out)
    throws IOException {

    }

    /** Computes a JAR diff of the two JAR files. */
    public Diff computeDiff() throws IOException {
        final SortedMap<String, EntryDigest>
                removed = new TreeMap<>(),
                added = new TreeMap<>(),
                unchanged = new TreeMap<>();
        final SortedMap<String, BeforeAndAfterEntryDigest>
                changed = new TreeMap<>();
        class JarVisitor implements Visitor<IOException> {
            @Override
            public void visitEntryInFile1(EntryInFile entryInFile1)
            throws IOException {
                final String name1 = entryInFile1.entry().getName();
                removed.put(name1, new EntryDigest(name1,
                        digestToHexString(entryInFile1)));
            }

            @Override
            public void visitEntryInFile2(EntryInFile entryInFile2)
            throws IOException {
                final String name2 = entryInFile2.entry().getName();
                added.put(name2, new EntryDigest(name2,
                        digestToHexString(entryInFile2)));
            }

            @Override
            public void visitEntriesInFiles(EntryInFile entryInFile1,
                                            EntryInFile entryInFile2)
            throws IOException {
                final String name1 = entryInFile1.entry().getName();
                assert name1.equals(entryInFile2.entry().getName());
                final String digest1 = digestToHexString(entryInFile1);
                final String digest2 = digestToHexString(entryInFile2);
                if (digest1.equals(digest2))
                    unchanged.put(name1, new EntryDigest(name1,
                            digest1));
                else
                    changed.put(name1, new BeforeAndAfterEntryDigest(name1,
                            digest1, digest2));
            }
        }
        new Engine(jarFile1(), jarFile2()).accept(new JarVisitor());
        {
            final Diff diff = new Diff();
            diff.algorithm = algorithm();
            diff.numBytes = numBytes();
            diff.removed = nonEmptyOrNull(removed);
            diff.added = nonEmptyOrNull(added);
            diff.unchanged = nonEmptyOrNull(unchanged);
            diff.changed = nonEmptyOrNull(changed);
            return diff;
        }
    }

    private String digestToHexString(Source source) throws IOException {
        return MessageDigests.digestToHexString(messageDigest(), source);
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

    /**
     * A builder for a JAR diff.
     * The default message digest is SHA-1.
     */
    public static class Builder {
        private JarFile jarFile1, jarFile2;
        private Sink patchFileSink;
        private MessageDigest messageDigest = MessageDigests.sha1();

        public Builder jarFile1(final JarFile jarFile1) {
            this.jarFile1 = requireNonNull(jarFile1);
            return this;
        }

        public Builder jarFile2(final JarFile jarFile2) {
            this.jarFile2 = requireNonNull(jarFile2);
            return this;
        }

        public Builder patchFileSink(final Sink patchFileSink) {
            this.patchFileSink = requireNonNull(patchFileSink);
            return this;
        }

        public Builder messageDigest(final MessageDigest messageDigest) {
            this.messageDigest = requireNonNull(messageDigest);
            return this;
        }

        public JarDiff build() {
            return build(jarFile1, jarFile2, patchFileSink, messageDigest);
        }

        private static JarDiff build(
                final JarFile jarFile1,
                final JarFile jarFile2,
                final Sink patchFileSink,
                final MessageDigest messageDigest) {
            requireNonNull(jarFile1);
            requireNonNull(jarFile2);
            requireNonNull(patchFileSink);
            requireNonNull(messageDigest);
            return new JarDiff() {
                @Override public JarFile jarFile1() { return jarFile1; }
                @Override public JarFile jarFile2() { return jarFile2; }
                @Override public Sink patchFileSink() { return patchFileSink; }
                @Override public MessageDigest messageDigest() {
                    return messageDigest;
                }
            };
        }
    }
}

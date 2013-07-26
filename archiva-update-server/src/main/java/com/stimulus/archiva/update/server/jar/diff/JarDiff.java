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
    protected abstract JarFile jar1();

    /** Returns the second JAR file. */
    protected abstract JarFile jar2();

    /** Returns the sink for writing the JAR patch file. */
    protected abstract Sink output();

    /** Returns the message digest. */
    protected abstract MessageDigest digest();

    /**
     * Computes a JAR diff of the two JAR files and generates a JAR patch file to the given sink.
     */
    public void generate() throws IOException {
        final Diff diff = compute();
        try (ZipOutputStream out = new ZipOutputStream(output().output())) {
            out.setLevel(Deflater.BEST_COMPRESSION);
            out.putNextEntry(new ZipEntry("diff"));

        }
    }

    private static void serializeTo(final Diff diff, final @WillNotClose OutputStream out)
    throws IOException {

    }

    /** Computes a JAR diff of the two JAR files. */
    public Diff compute() throws IOException {
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
        new Engine(jar1(), jar2()).accept(new JarVisitor());
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
        return MessageDigests.digestToHexString(digest(), source);
    }

    private String algorithm() { return digest().getAlgorithm(); }

    private Integer numBytes() {
        final MessageDigest digest = digest();
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
        private JarFile jar1, jar2;
        private Sink output;
        private MessageDigest digest = MessageDigests.sha1();

        public Builder jar1(final JarFile jar1) {
            this.jar1 = requireNonNull(jar1);
            return this;
        }

        public Builder jar2(final JarFile jar2) {
            this.jar2 = requireNonNull(jar2);
            return this;
        }

        public Builder output(final Sink output) {
            this.output = requireNonNull(output);
            return this;
        }

        public Builder digest(final MessageDigest digest) {
            this.digest = requireNonNull(digest);
            return this;
        }

        public JarDiff build() { return build(jar1, jar2, output, digest); }

        private static JarDiff build(
                final JarFile jar1,
                final JarFile jar2,
                final Sink output,
                final MessageDigest digest) {
            requireNonNull(jar1);
            requireNonNull(jar2);
            requireNonNull(output);
            requireNonNull(digest);
            return new JarDiff() {
                @Override protected JarFile jar1() { return jar1; }
                @Override protected JarFile jar2() { return jar2; }
                @Override protected Sink output() { return output; }
                @Override protected MessageDigest digest() { return digest; }
            };
        }
    }
}

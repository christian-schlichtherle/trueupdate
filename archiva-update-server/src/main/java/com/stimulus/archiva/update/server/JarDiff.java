/*
 * Copyright (C) 2005-2013 Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.server;

import edu.umd.cs.findbugs.annotations.CreatesObligation;
import java.io.*;
import java.util.*;
import static java.util.Collections.unmodifiableCollection;
import java.util.jar.*;
import javax.annotation.WillNotClose;
import javax.annotation.concurrent.Immutable;

/**
 * Computes a diff of two JAR files.
 *
 * @author Christian Schlichtherle
 */
@Immutable
public final class JarDiff {

    private JarDiff() { }

    /**
     *  Computes a diff of two JAR files.
     *
     *  @param file1 the first JAR file.
     *  @param file2 the second JAR file.
     *  @return the diff result.
     */
    public static Result compute(
            final @WillNotClose JarFile file1,
            final @WillNotClose JarFile file2) {
        final SortedMap<String, Fingerprint>
                entriesOnlyInFile1 = new TreeMap<>(),
                entriesOnlyInFile2 = new TreeMap<>();
        final SortedMap<String, Pair> differentEntries = new TreeMap<>();
        new Engine(file1, file2) {
            @Override void onEntryOnlyInFile1(Fingerprint fingerprint1) {
                entriesOnlyInFile1.put(fingerprint1.name(), fingerprint1);
            }

            @Override void onEntryOnlyInFile2(Fingerprint fingerprint2) {
                entriesOnlyInFile2.put(fingerprint2.name(), fingerprint2);
            }

            @Override void onDifferentEntries(
                    Fingerprint fingerprint1,
                    Fingerprint fingerprint2) {
                differentEntries.put(fingerprint1.name(),
                        new Pair(fingerprint1, fingerprint2));
            }
        }.run();
        return new Result() {
            @Override public Collection<Fingerprint> entriesOnlyInFile1() {
                return unmodifiableCollection(entriesOnlyInFile1.values());
            }

            @Override public Collection<Fingerprint> entriesOnlyInFile2() {
                return unmodifiableCollection(entriesOnlyInFile2.values());
            }

            @Override public Collection<Pair> differentEntries() {
                return unmodifiableCollection(differentEntries.values());
            }
        };
    }

    /**
     * Computes a diff of two JAR files.
     * Clients need to override some of the template methods {@code on...()} in
     * order to implement the actions.
     */
    @Immutable
    abstract static class Engine {

        final @WillNotClose JarFile file1, file2;

        Engine( final @WillNotClose JarFile file1,
                final @WillNotClose JarFile file2) {
            assert null != file1;
            this.file1 = file1;
            assert null != file2;
            this.file2 = file2;
        }

        /**
         * Computes the diff and calls the template methods where appropriate.
         * Note that the order of the calls to the template methods is
         * undefined, so you should not depend on the behavior of the current
         * implementation in order to ensure compatibility with future versions.
         */
        final void run() {
            for (final Enumeration<JarEntry> e1 = file1.entries();
                 e1.hasMoreElements(); ) {
                final JarEntry entry1 = e1.nextElement();
                final JarEntry entry2 = file2.getJarEntry(entry1.getName());
                final Fingerprint fp1 = new Fingerprint(file1, entry1);
                if (null == entry2) {
                    onEntryOnlyInFile1(fp1);
                } else {
                    final Fingerprint fp2 = new Fingerprint(file2, entry2);
                    if (fp1.quickEquals(fp2)) {
                        assert fp1.equals(fp2);
                        onEqualEntries(fp1, fp2);
                    } else {
                        assert !fp1.equals(fp2);
                        onDifferentEntries(fp1, fp2);
                    }
                }
            }

            for (final Enumeration<JarEntry> e2 = file2.entries();
                 e2.hasMoreElements(); ) {
                final JarEntry entry2 = e2.nextElement();
                final JarEntry entry1 = file1.getJarEntry(entry2.getName());
                if (null == entry1)
                    onEntryOnlyInFile2(new Fingerprint(file2, entry2));
            }
        }

        /**
         * Called for each JAR entry which is present in {@link #file1}, but not
         * in {@link #file2}.
         *
         * @param fingerprint1 the fingerprint for the JAR entry in the first file.
         */
        void onEntryOnlyInFile1(Fingerprint fingerprint1) {
            assert null != fingerprint1;
        }

        /**
         * Called for each JAR entry which is present in {@link #file2}, but not
         * in {@link #file1}.
         *
         * @param fingerprint2 the fingerprint for the JAR entry in the second file.
         */
        void onEntryOnlyInFile2(Fingerprint fingerprint2) {
            assert null != fingerprint2;
        }

        /**
         * Called for each pair of JAR entries with an equal name in
         * {@link #file1} and {@link #file2}
         * and with equal {@link Fingerprint}s.
         *
         * @param fingerprint1 the fingerprint for the JAR entry in the first file.
         * @param fingerprint2 the fingerprint for the JAR entry in the second file.
         */
        void onEqualEntries(Fingerprint fingerprint1, Fingerprint fingerprint2) {
            assert fingerprint1.name().equals(fingerprint2.name());
            assert fingerprint1.equals(fingerprint2);
        }

        /**
         * Called for each pair of JAR entries with an equal name in
         * {@link #file1} and {@link #file2},
         * but with different {@link Fingerprint}s.
         *
         * @param fingerprint1 the fingerprint for the JAR entry in the first file.
         * @param fingerprint2 the fingerprint for the JAR entry in the second file.
         */
        void onDifferentEntries(Fingerprint fingerprint1, Fingerprint fingerprint2) {
            assert fingerprint1.name().equals(fingerprint2.name());
            assert !fingerprint1.equals(fingerprint2);
        }
    } // Engine

    /**
     * The result of diffing two JAR files.
     * Note that the returned collections are unmodifiable.
     */
    public interface Result {

        /**
         * Returns an unmodifiable collection of fingerprints of the entries
         * which are only present in the first JAR file.
         * The fingerprints in the collection are sorted in the natural order
         * of the corresponding entry names.
         */
        Collection<Fingerprint> entriesOnlyInFile1();

        /**
         * Returns an unmodifiable collection of fingerprints of the entries
         * which are only present in the second JAR file.
         * The fingerprints in the collection are sorted in the natural order
         * of the corresponding entry names.
         */
        Collection<Fingerprint> entriesOnlyInFile2();

        /**
         * Returns an unmodifiable collection of pairs of fingerprints of the
         * entries which are different in both JAR files.
         * Note that although the fingerprints are different, the corresponding
         * entry names are equal, i.e. the entries are present in both JAR
         * files.
         * The fingerprints in the collection are sorted in the natural order
         * of the corresponding entry names.
         */
        Collection<Pair> differentEntries();
    } // Result

    /**
     * A pair of JAR entry fingerprints in two different JAR files.
     * The fingerprints may be different, but they are guaranteed to have an
     * equal entry name.
     */
    @Immutable
    public final static class Pair {

        private final Fingerprint fingerprint1, fingerprint2;

        Pair(final Fingerprint fingerprint1, final Fingerprint fingerprint2) {
            assert fingerprint1.name().equals(fingerprint2.name());
            this.fingerprint1 = fingerprint1;
            this.fingerprint2 = fingerprint2;
        }

        /** Returns the fingerprint of the JAR entry in the first file. */
        public Fingerprint fingerprint1() { return fingerprint1; }

        /** Returns the fingerprint of the JAR entry in the second file. */
        public Fingerprint fingerprint2() { return fingerprint2; }

        @Override public String toString() {
            return String.format("%s@%x[fingerprint1=%s, fingerprint2=%s]",
                    getClass().getName(),
                    hashCode(),
                    fingerprint1(),
                    fingerprint2());
        }

        /**
         * Returns {@code true} if and only if the given object is an instance
         * of this class with equal {@link #fingerprint1()} and
         * {@link #fingerprint2()}.
         */
        @Override public boolean equals(final Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof Pair)) return false;
            final Pair that = (Pair) obj;
            return this.fingerprint1().equals(that.fingerprint1()) &&
                    this.fingerprint2().equals(that.fingerprint2());
        }

        /**
         * Returns a hash code which is consistent with {@link #equals(Object)}.
         */
        @Override public int hashCode() {
            int hash = 17;
            hash = 31 * hash + fingerprint1().hashCode();
            hash = 31 * hash + fingerprint2().hashCode();
            return hash;
        }
    } // Pair

    /** The fingerprint of an entry in a JAR file. */
    @Immutable
    public final static class Fingerprint {

        private final JarFile file;
        private final JarEntry entry;

        Fingerprint(final JarFile file, final JarEntry entry) {
            assert null != file;
            this.file = file;
            assert null != entry;
            this.entry = entry;
        }

        /** Returns a new input stream for reading the corresponding entry. */
        public @CreatesObligation
        InputStream inputStream() throws IOException {
            return file.getInputStream(entry);
        }

        /** Returns the entry name. */
        public String name() { return entry.getName(); }

        /** Returns the last modification time of the entry. */
        public long time() { return entry.getTime(); }

        /** Returns the size of the entry. */
        public long size() { return entry.getSize(); }

        /** Returns the CRC-32 checksum of the entry. */
        public long crc32() { return entry.getCrc(); }

        @Override public String toString() {
            return String.format("%s@%x[name=\"%s\", time=%tc, size=%d, crc32=%d]",
                    getClass().getName(),
                    hashCode(),
                    name(),
                    time(),
                    size(),
                    crc32());
        }

        /**
         * Returns {@code true} if and only if the given object is an instance
         * of this class with equal {@link #name()}, {@link #time()},
         * {@link #size()} and {@link #crc32()}.
         */
        @Override public boolean equals(final Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof Fingerprint)) return false;
            final Fingerprint that = (Fingerprint) obj;
            return this.name().equals(that.name()) && this.quickEquals(that);
        }

        boolean quickEquals(final Fingerprint that) {
            assert this.name().equals(that.name());
            return this.time() == that.time() &&
                    this.size() == that.size() &&
                    this.crc32() == that.crc32();
        }

        /**
         * Returns a hash code which is consistent with {@link #equals(Object)}.
         */
        @Override public int hashCode() {
            int hash = 17;
            hash = 31 * hash + name().hashCode();
            hash = 31 * hash + hashCode(time());
            hash = 31 * hash + hashCode(size());
            hash = 31 * hash + hashCode(crc32());
            return hash;
        }

        private static int hashCode(long value) {
            return (int) (value ^ (value >>> 32)); // stolen from Long.hashCode()
        }
    } // Fingerprint
}

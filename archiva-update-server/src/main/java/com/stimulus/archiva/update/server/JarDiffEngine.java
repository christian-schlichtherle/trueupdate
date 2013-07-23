/*
 * Copyright (C) 2005-2013 Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.server;

import edu.umd.cs.findbugs.annotations.CreatesObligation;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import javax.annotation.WillNotClose;
import javax.annotation.concurrent.Immutable;

/**
 * Diffs two JAR files.
 * Clients need to implement the abstract property methods in order to
 * provide the JAR files to diff and override some of the template methods
 * {@code on...()} in order to implement diff behaviour.
 *
 * @author Christian Schlichtherle
 */
@Immutable
public abstract class JarDiffEngine implements Callable<Void> {

    /** Returns the first JAR file. */
    protected abstract @WillNotClose JarFile file1();

    /** Returns the second JAR file. */
    protected abstract @WillNotClose JarFile file2();

    /**
     * Computes the diff and calls the template methods where appropriate.
     * Note that the order in which the template methods get called is
     * undefined, so you should not depend on the behavior of the current
     * implementation in order to ensure compatibility with future changes.
     *
     * @throws Exception at the discretion of any template method, which will
     *         abort the computation of the diff.
     */
    public Void call() throws Exception {
        final JarFile file1 = file1(), file2 = file2();

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

        return null;
    }

    /**
     * Called for each JAR entry which is present in {@link #file1()}, but not
     * in {@link #file2()}.
     *
     * @param fingerprint1 the fingerprint for the JAR entry in the first file.
     * @throws Exception at the discretion of the implementation, which will
     *         abort the computation of the diff.
     */
    protected void onEntryOnlyInFile1(Fingerprint fingerprint1)
    throws Exception {
        assert null != fingerprint1;
    }

    /**
     * Called for each JAR entry which is present in {@link #file2()}, but not
     * in {@link #file1()}.
     *
     * @param fingerprint2 the fingerprint for the JAR entry in the second file.
     * @throws Exception at the discretion of the implementation, which will
     *         abort the computation of the diff.
     */
    protected void onEntryOnlyInFile2(Fingerprint fingerprint2)
    throws Exception {
        assert null != fingerprint2;
    }

    /**
     * Called for each pair of JAR entries with an equal name in
     * {@link #file1()} and {@link #file2()}
     * and with equal {@link Fingerprint}s.
     *
     * @param fingerprint1 the fingerprint for the JAR entry in the first file.
     * @param fingerprint2 the fingerprint for the JAR entry in the second file.
     * @throws Exception at the discretion of the implementation, which will
     *         abort the computation of the diff.
     */
    protected void onEqualEntries(Fingerprint fingerprint1, Fingerprint fingerprint2)
    throws Exception {
        assert fingerprint1.name().equals(fingerprint2.name());
        assert fingerprint1.equals(fingerprint2);
    }

    /**
     * Called for each pair of JAR entries with an equal name in
     * {@link #file1()} and {@link #file2()},
     * but with different {@link Fingerprint}s.
     *
     * @param fingerprint1 the fingerprint for the JAR entry in the first file.
     * @param fingerprint2 the fingerprint for the JAR entry in the second file.
     * @throws Exception at the discretion of the implementation, which will
     *         abort the computation of the diff.
     */
    protected void onDifferentEntries(Fingerprint fingerprint1, Fingerprint fingerprint2)
    throws Exception {
        assert fingerprint1.name().equals(fingerprint2.name());
        assert !fingerprint1.equals(fingerprint2);
    }

    protected final static class Fingerprint {

        final JarFile file;
        final JarEntry entry;

        Fingerprint(final JarFile file, final JarEntry entry) {
            this.file = file;
            this.entry = entry;
        }

        /** Returns a new input stream for reading the underlying entry. */
        public @CreatesObligation InputStream inputStream() throws IOException {
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
    }
}

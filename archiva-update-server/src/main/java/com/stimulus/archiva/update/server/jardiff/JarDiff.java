package com.stimulus.archiva.update.server.jardiff;

import edu.umd.cs.findbugs.annotations.CreatesObligation;

import javax.annotation.WillNotClose;
import javax.annotation.concurrent.Immutable;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Diffs two JAR files.
 * Clients need to implement the abstract property methods in order to
 * provide the JAR files to diff and override some of the {@code on...}
 * template methods in order to implement diff behaviour.
 *
 * @author Christian Schlichtherle
 */
@Immutable
public abstract class JarDiff implements Callable<Void> {

    /** Returns the first JAR file. */
    protected abstract @WillNotClose JarFile file1();

    /** Returns the second JAR file. */
    protected abstract @WillNotClose JarFile file2();

    /**
     * Computes the diff and calls the template methods where appropriate.
     * To ensure future compatibility, the order in which the template methods
     * get called is undefined, so you should <em>not</em> depend on the
     * behavior of the current implementation.
     */
    public Void call() throws Exception {
        final NavigableMap<String, Fingerprint>
                fingerprints1 = fingerprints(file1());
        final NavigableMap<String, Fingerprint>
                fingerprints2 = fingerprints(file2());

        for (final Map.Entry<String, Fingerprint> entry1
                : fingerprints1.entrySet()) {
            final String entryName1 = entry1.getKey();
            final Fingerprint fingerprint1 = entry1.getValue();
            final Fingerprint fingerprint2 = fingerprints2.get(entryName1);
            if (null == fingerprint2)
                onEntryOnlyInFile1(fingerprint1);
            else if (fingerprint1.equals(fingerprint2))
                onEqualEntries(fingerprint1, fingerprint2);
            else
                onDifferentEntries(fingerprint1, fingerprint2);
        }

        for (final Map.Entry<String, Fingerprint> entry2
                : fingerprints2.entrySet()) {
            final String entryName2 = entry2.getKey();
            if (!fingerprints1.containsKey(entryName2))
                onEntryOnlyInFile2(entry2.getValue());
        }

        return null;
    }

    private static NavigableMap<String, Fingerprint> fingerprints(final JarFile file) {
        final Enumeration<JarEntry> e = file.entries();
        final NavigableMap<String, Fingerprint> map = new TreeMap<>();
        while (e.hasMoreElements()) {
            final JarEntry entry = e.nextElement();
            map.put(entry.getName(), new Fingerprint(file, entry));
        }
        return map;
    }

    protected void onEntryOnlyInFile1(Fingerprint fingerprint1)
    throws Exception { }

    protected void onEntryOnlyInFile2(Fingerprint fingerprint2)
    throws Exception { }

    protected void onEqualEntries(Fingerprint fingerprint1, Fingerprint fingerprint2)
    throws Exception { }

    protected void onDifferentEntries(Fingerprint fingerprint1, Fingerprint fingerprint2)
    throws Exception { }

    protected final static class Fingerprint {

        final JarFile file;
        final JarEntry entry;

        Fingerprint(final JarFile file, final JarEntry entry) {
            this.file = file;
            this.entry = entry;
        }

        public String name() { return entry.getName(); }
        public long time() { return entry.getTime(); }
        public long size() { return entry.getSize(); }
        public long crc32() { return entry.getCrc(); }

        public @CreatesObligation InputStream inputStream() throws IOException {
            return file.getInputStream(entry);
        }

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
         * of this class with equal properties.
         */
        @Override public boolean equals(final Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof Fingerprint)) return false;
            final Fingerprint that = (Fingerprint) obj;
            return this.name().equals(that.name()) &&
                    this.time() == that.time() &&
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

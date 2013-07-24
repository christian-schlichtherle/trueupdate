/*
 * Copyright (C) 2005-2013 Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.server;

import java.util.*;
import static java.util.Objects.requireNonNull;
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

    private final Comparator comparator;

    /**
     * Constructs a JAR diff.
     *
     * @param comparator the comparator for testing two JAR entries in
     *                   different JAR files for equality.
     */
    public JarDiff(final Comparator comparator) {
        this.comparator = requireNonNull(comparator);
    }

    /**
     *  Computes a diff of the two given JAR files.
     *
     *  @param file1 the first JAR file.
     *  @param file2 the second JAR file.
     *  @return the diff result.
     */
    public Result compute(
            final @WillNotClose JarFile file1,
            final @WillNotClose JarFile file2) {
        final SortedMap<String, EntryInFile>
                entriesOnlyInFile1 = new TreeMap<>(),
                entriesOnlyInFile2 = new TreeMap<>();
        final SortedMap<String, PairOfEntriesInFiles>
                differentEntries = new TreeMap<>();
        new Engine(file1, file2) {
            @Override void onEntryOnlyInFile1(EntryInFile entryInFile1) {
                entriesOnlyInFile1.put(
                        entryInFile1.entry().getName(),
                        entryInFile1);
            }

            @Override void onEntryOnlyInFile2(EntryInFile entryInFile2) {
                entriesOnlyInFile2.put(
                        entryInFile2.entry().getName(),
                        entryInFile2);
            }

            @Override void onDifferentEntries(
                    EntryInFile entryInFile1,
                    EntryInFile entryInFile2) {
                assert entryInFile1.entry().getName().equals(
                        entryInFile2.entry().getName());
                differentEntries.put(
                        entryInFile1.entry().getName(),
                        pairOfEntriesInFiles(entryInFile1, entryInFile2));
            }
        }.run();
        return new Result() {
            @Override public Collection<EntryInFile> entriesOnlyInFile1() {
                return unmodifiableCollection(entriesOnlyInFile1.values());
            }

            @Override public Collection<EntryInFile> entriesOnlyInFile2() {
                return unmodifiableCollection(entriesOnlyInFile2.values());
            }

            @Override public Collection<PairOfEntriesInFiles> differentEntries() {
                return unmodifiableCollection(differentEntries.values());
            }
        };
    }

    private static PairOfEntriesInFiles pairOfEntriesInFiles(
            final EntryInFile entryInFile1,
            final EntryInFile entryInFile2) {
        return new PairOfEntriesInFiles() {
            public EntryInFile entryInFile1() { return entryInFile1; }
            public EntryInFile entryInFile2() { return entryInFile2; }
        };
    }

    private static EntryInFile entryInFile(
            final JarEntry entry,
            final JarFile file) {
        return new EntryInFile() {
            public JarEntry entry() { return entry; }
            public JarFile file() { return file; }
        };
    }

    /**
     * Computes a diff of two JAR files.
     * Clients need to override some of the template methods {@code on...()} in
     * order to implement the actions.
     */
    @Immutable
    abstract class Engine {

        final @WillNotClose JarFile file1, file2;

        Engine( final @WillNotClose JarFile file1,
                final @WillNotClose JarFile file2) {
            this.file1 = requireNonNull(file1);
            this.file2 = requireNonNull(file2);
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
                if (null == entry2) {
                    onEntryOnlyInFile1(entryInFile(entry1, file1));
                } else {
                    final EntryInFile entryInFile1 = entryInFile(entry1, file1);
                    final EntryInFile entryInFile2 = entryInFile(entry2, file2);
                    if (comparator.equals(entryInFile1, entryInFile2))
                        onEqualEntries(entryInFile1, entryInFile2);
                    else
                        onDifferentEntries(entryInFile1, entryInFile2);
                }
            }

            for (final Enumeration<JarEntry> e2 = file2.entries();
                 e2.hasMoreElements(); ) {
                final JarEntry entry2 = e2.nextElement();
                final JarEntry entry1 = file1.getJarEntry(entry2.getName());
                if (null == entry1)
                    onEntryOnlyInFile2(entryInFile(entry2, file2));
            }
        }

        /**
         * Called for each JAR entry which is present in the first JAR file,
         * but not in the second JAR file.
         *
         * @param entryInFile1 the JAR entry in the first JAR file.
         */
        void onEntryOnlyInFile1(EntryInFile entryInFile1) {
            assert null != entryInFile1;
        }

        /**
         * Called for each JAR entry which is present in the second JAR file,
         * but not in the first JAR file.
         *
         * @param entryInFile2 the JAR entry in the second JAR file.
         */
        void onEntryOnlyInFile2(EntryInFile entryInFile2) {
            assert null != entryInFile2;
        }

        /**
         * Called for each pair of JAR entries with an equal name in
         * the first and second JAR file
         * and which are considered to be equal.
         *
         * @param entryInFile1 the JAR entry in the first JAR file.
         * @param entryInFile2 the JAR entry in the second JAR file.
         */
        void onEqualEntries(EntryInFile entryInFile1, EntryInFile entryInFile2) {
            assert entryInFile1.entry().getName().equals(
                    entryInFile2.entry().getName());
        }

        /**
         * Called for each pair of JAR entries with an equal name in
         * the first and second JAR file,
         * but which are considered to be different.
         *
         * @param entryInFile1 the JAR entry in the first JAR file.
         * @param entryInFile2 the JAR entry in the second JAR file.
         */
        void onDifferentEntries(EntryInFile entryInFile1, EntryInFile entryInFile2) {
            assert entryInFile1.entry().getName().equals(
                    entryInFile2.entry().getName());
        }
    } // Engine

    /** Compares two JAR entries in different JAR files. */
    public interface Comparator {
        /**
         * Returns {@code true} if and only if the two given JAR entries in
         * different JAR files should be considered to be equal.
         * Note that it's the client's responsibility to ensure that the entry
         * names are equal before calling this method so that the
         * implementation can safely assume this without testing again.
         *
         * @param entryInFile1 the JAR entry in the first JAR file.
         * @param entryInFile2 the JAR entry in the second JAR file.
         */
        boolean equals(EntryInFile entryInFile1, EntryInFile entryInFile2);
    } // Comparator

    /** A result of diffing two JAR files. */
    public interface Result {
        /**
         * Returns an unmodifiable collection of JAR entries which are only
         * present in the first JAR file.
         * The collection is sorted according to the natural order of the JAR
         * entry names.
         */
        Collection<EntryInFile> entriesOnlyInFile1();

        /**
         * Returns an unmodifiable collection of JAR entries which are only
         * present in the second JAR file.
         * The collection is sorted according to the natural order of the JAR
         * entry names.
         */
        Collection<EntryInFile> entriesOnlyInFile2();

        /**
         * Returns an unmodifiable collection of pairs of JAR entries with
         * equal names in both JAR files, but which are considered to be
         * different.
         * The collection is sorted according to the natural order of the JAR
         * entry names.
         */
        Collection<PairOfEntriesInFiles> differentEntries();
    } // Result

    /** A pair of JAR entries with equal names in different JAR files. */
    public interface PairOfEntriesInFiles {
        /** Returns the JAR entry in the first JAR file. */
        EntryInFile entryInFile1();

        /** Returns the JAR entry in the second JAR file. */
        EntryInFile entryInFile2();
    } // PairOfEntriesInFiles

    /** A JAR entry in a JAR file. */
    public interface EntryInFile {
        /** Returns the JAR entry. */
        JarEntry entry();

        /** Returns the JAR file. */
        JarFile file();
    } // EntryInFile
}

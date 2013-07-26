/*
 * Copyright (C) 2005-2013 Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.server.jardiff;

import com.stimulus.archiva.update.server.jardiff.model.*;
import com.stimulus.archiva.update.server.jardiff.model.Comparator;
import java.util.*;
import static java.util.Objects.requireNonNull;
import java.util.jar.*;
import javax.annotation.WillNotClose;
import javax.annotation.concurrent.Immutable;

/**
 * Computes a diff of two JAR files.
 *
 * @param <X> the type of the exceptions to be thrown from the
 *        {@link Comparator} or {@link Visitor}.
 * @author Christian Schlichtherle
 */
@Immutable
public final class JarDiff<X extends Exception> {

    private final Comparator<X> comparator;

    /**
     * Constructs a JAR diff.
     *
     * @param comparator the comparator for testing two JAR entries in
     *                   different JAR files for equality.
     *                   The implementation can safely assume that the entry
     *                   names are equal without testing.
     *
     */
    public JarDiff(final Comparator<X> comparator) {
        this.comparator = requireNonNull(comparator);
    }

    /**
     * Computes a {@link Diff} of the two given JAR files.
     *
     * @param file1 the first JAR file.
     * @param file2 the second JAR file.
     * @return the diff result.
     * @throws X at the discretion of the {@link Comparator}, in which case the
     *         diff gets aborted.
     */
    public Diff compute(
            final @WillNotClose JarFile file1,
            final @WillNotClose JarFile file2)
    throws X {
        final SortedMap<String, EntryInFile>
                entriesInFile1 = new TreeMap<>(),
                entriesInFile2 = new TreeMap<>();
        final SortedMap<String, PairOfEntriesInFiles>
                equalEntries = new TreeMap<>(),
                differentEntries = new TreeMap<>();
        class JarVisitor implements Visitor<X> {
            @Override public void visitEntryInFile1(EntryInFile entryInFile1) {
                entriesInFile1.put(
                        entryInFile1.entry().getName(),
                        entryInFile1);
            }

            @Override public void visitEntryInFile2(EntryInFile entryInFile2) {
                entriesInFile2.put(
                        entryInFile2.entry().getName(),
                        entryInFile2);
            }

            @Override public void visitEqualEntries(EntryInFile entryInFile1,
                                                    EntryInFile entryInFile2) {
                assert entryInFile1.entry().getName().equals(
                        entryInFile2.entry().getName());
                equalEntries.put(
                        entryInFile1.entry().getName(),
                        new PairOfEntriesInFiles(entryInFile1, entryInFile2));
            }

            @Override public void visitDifferentEntries(EntryInFile entryInFile1,
                                                        EntryInFile entryInFile2) {
                assert entryInFile1.entry().getName().equals(
                        entryInFile2.entry().getName());
                differentEntries.put(
                        entryInFile1.entry().getName(),
                        new PairOfEntriesInFiles(entryInFile1, entryInFile2));
            }
        }
        engine(file1, file2).accept(new JarVisitor());
        return new Diff(comparator,
                entriesInFile1, entriesInFile2, equalEntries, differentEntries);
    }

    public Engine engine(@WillNotClose JarFile file1,
                         @WillNotClose JarFile file2) {
        return new Engine(file1, file2);
    }

    /** Computes a diff of two JAR files. */
    @Immutable
    public final class Engine {

        private final @WillNotClose JarFile file1, file2;

        /**
         * Constructs a JAR diff engine which operates on the given JAR files.
         *
         * @param file1 the first JAR file.
         * @param file2 the second JAR file.
         */
        Engine(final @WillNotClose JarFile file1,
               final @WillNotClose JarFile file2) {
            assert null != file1;
            this.file1 = file1;
            assert null != file2;
            this.file2 = file2;
        }

        /**
         * Computes the diff and calls the visitor methods where appropriate.
         * Note that this method ensures that the entry names are equal before
         * calling {@link Comparator#equals(EntryInFile, EntryInFile)}.
         *
         * @param visitor the visitor for the JAR entries in the JAR files.
         * @throws X at the discretion of the {@link Comparator} or
         *         {@link Visitor}, in which case the diff gets aborted.
         */
        public void accept(final Visitor<X> visitor) throws X {
            for (final Enumeration<JarEntry> e1 = file1.entries();
                 e1.hasMoreElements(); ) {
                final JarEntry entry1 = e1.nextElement();
                final JarEntry entry2 = file2.getJarEntry(entry1.getName());
                if (null == entry2) {
                    visitor.visitEntryInFile1(new EntryInFile(entry1, file1));
                } else {
                    final EntryInFile entryInFile1 = new EntryInFile(entry1, file1);
                    final EntryInFile entryInFile2 = new EntryInFile(entry2, file2);
                    if (comparator.equals(entryInFile1, entryInFile2))
                        visitor.visitEqualEntries(entryInFile1, entryInFile2);
                    else
                        visitor.visitDifferentEntries(entryInFile1, entryInFile2);
                }
            }

            for (final Enumeration<JarEntry> e2 = file2.entries();
                 e2.hasMoreElements(); ) {
                final JarEntry entry2 = e2.nextElement();
                final JarEntry entry1 = file1.getJarEntry(entry2.getName());
                if (null == entry1)
                    visitor.visitEntryInFile2(new EntryInFile(entry2, file2));
            }
        }
    } // Engine
}

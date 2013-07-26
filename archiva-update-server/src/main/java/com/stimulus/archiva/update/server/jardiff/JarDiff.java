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

            @Override public void visitEntriesInFiles(EntryInFile entryInFile1,
                                                      EntryInFile entryInFile2)
            throws X {
                final String name1 = entryInFile1.entry().getName();
                assert name1.equals(entryInFile2.entry().getName());
                if (comparator.equals(entryInFile1, entryInFile2))
                    equalEntries.put(name1,
                            new PairOfEntriesInFiles(entryInFile1, entryInFile2));
                else
                    differentEntries.put(name1,
                            new PairOfEntriesInFiles(entryInFile1, entryInFile2));
            }
        }
        new JarVisitorEngine(file1, file2).accept(new JarVisitor());
        return new Diff(comparator,
                entriesInFile1, entriesInFile2, equalEntries, differentEntries);
    }
}

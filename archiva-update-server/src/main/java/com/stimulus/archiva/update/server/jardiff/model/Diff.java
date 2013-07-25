/*
 * Copyright (C) 2005-2013 Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.server.jardiff.model;

import java.util.Collection;
import static java.util.Objects.requireNonNull;
import java.util.SortedMap;

/**
 * Represents the result of diffing two JAR files.
 *
 * @author Christian Schlichtherle
 */
public final class Diff {

    private final Comparator comparator;

    private final Collection<EntryInFile>
            entriesInFile1, entriesInFile2;

    private final Collection<PairOfEntriesInFiles>
            equalEntries, differentEntries;

    public Diff(
            final Comparator comparator,
            final SortedMap<String, EntryInFile> entriesInFile1,
            final SortedMap<String, EntryInFile> entriesInFile2,
            final SortedMap<String, PairOfEntriesInFiles> equalEntries,
            final SortedMap<String, PairOfEntriesInFiles> differentEntries) {
        this.comparator = requireNonNull(comparator);
        this.entriesInFile1 = entriesInFile1.values();
        this.entriesInFile2 = entriesInFile2.values();
        this.equalEntries = equalEntries.values();
        this.differentEntries = differentEntries.values();
    }

    /** Returns the comparator which was used to create these results. */
    public Comparator comparator() { return comparator; }

    /**
     * Returns a collection of JAR entries which are only present in the first
     * JAR file.
     * The collection is sorted according to the natural order of the JAR
     * entry names.
     * Clients should not modify the returned collection.
     */
    public Collection<EntryInFile> entriesInFile1() { return entriesInFile1; }

    /**
     * Returns a collection of JAR entries which are only present in the second
     * JAR file.
     * The collection is sorted according to the natural order of the JAR
     * entry names.
     * Clients should not modify the returned collection.
     */
    public Collection<EntryInFile> entriesInFile2() { return entriesInFile2; }

    /**
     * Returns a collection of pairs of JAR entries with equal names in both
     * JAR files and which are considered to be equal according to the
     * {@link #comparator()}.
     * The collection is sorted according to the natural order of the JAR
     * entry names.
     * Clients should not modify the returned collection.
     */
    public Collection<PairOfEntriesInFiles> equalEntries() {
        return equalEntries;
    }

    /**
     * Returns a collection of pairs of JAR entries with equal names in both
     * JAR files, but which are considered to be different according to the
     * {@link #comparator()}.
     * The collection is sorted according to the natural order of the JAR
     * entry names.
     * Clients should not modify the returned collection.
     */
    public Collection<PairOfEntriesInFiles> differentEntries() {
        return differentEntries;
    }
}

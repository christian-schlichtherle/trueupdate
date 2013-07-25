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

    private final SortedMap<String, EntryInFile>
            entriesInFile1, entriesInFile2;

    private final SortedMap<String, PairOfEntriesInFiles>
            equalEntries, differentEntries;

    public Diff(
            final SortedMap<String, EntryInFile> entriesInFile1,
            final SortedMap<String, EntryInFile> entriesInFile2,
            final SortedMap<String, PairOfEntriesInFiles> equalEntries,
            final SortedMap<String, PairOfEntriesInFiles> differentEntries) {
        this.entriesInFile1 = requireNonNull(entriesInFile1);
        this.entriesInFile2 = requireNonNull(entriesInFile2);
        this.equalEntries = requireNonNull(equalEntries);
        this.differentEntries = requireNonNull(differentEntries);
    }

    /**
     * Returns a collection of JAR entries which are only present in the first
     * JAR file.
     * The collection is sorted according to the natural order of the JAR
     * entry names.
     * Clients should not modify the returned collection.
     */
    public Collection<EntryInFile> entriesInFile1() {
        return entriesInFile1.values();
    }

    /**
     * Returns a collection of JAR entries which are only present in the second
     * JAR file.
     * The collection is sorted according to the natural order of the JAR
     * entry names.
     * Clients should not modify the returned collection.
     */
    public Collection<EntryInFile> entriesInFile2() {
        return entriesInFile2.values();
    }

    /**
     * Returns a collection of pairs of JAR entries with equal names in both
     * JAR files and which are considered to be equal.
     * The collection is sorted according to the natural order of the JAR
     * entry names.
     * Clients should not modify the returned collection.
     */
    public Collection<PairOfEntriesInFiles> equalEntries() {
        return equalEntries.values();
    }

    /**
     * Returns a collection of pairs of JAR entries with equal names in both
     * JAR files, but which are considered to be different.
     * The collection is sorted according to the natural order of the JAR
     * entry names.
     * Clients should not modify the returned collection.
     */
    public Collection<PairOfEntriesInFiles> differentEntries() {
        return differentEntries.values();
    }
}

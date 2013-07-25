/*
 * Copyright (C) 2005-2013 Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.server.jardiff.model;

import java.util.Collection;

/**
 * The result of diffing two JAR files.
 *
 * @author Christian Schlichtherle
 */
public interface Diff {
    /**
     * Returns an unmodifiable collection of JAR entries which are only
     * present in the first JAR file.
     * The collection is sorted according to the natural order of the JAR
     * entry names.
     */
    Collection<EntryInFile> entriesInFile1();

    /**
     * Returns an unmodifiable collection of JAR entries which are only
     * present in the second JAR file.
     * The collection is sorted according to the natural order of the JAR
     * entry names.
     */
    Collection<EntryInFile> entriesInFile2();

    /**
     * Returns an unmodifiable collection of pairs of JAR entries with
     * equal names in both JAR files and which are considered to be
     * equal according to the {@link Comparator}.
     * The collection is sorted according to the natural order of the JAR
     * entry names.
     */
    Collection<PairOfEntriesInFiles> equalEntries();

    /**
     * Returns an unmodifiable collection of pairs of JAR entries with
     * equal names in both JAR files, but which are considered to be
     * different according to the {@link Comparator}.
     * The collection is sorted according to the natural order of the JAR
     * entry names.
     */
    Collection<PairOfEntriesInFiles> differentEntries();
}

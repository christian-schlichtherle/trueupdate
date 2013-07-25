/*
 * Copyright (C) 2005-2013 Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.server.jardiff.model;

import static java.util.Objects.requireNonNull;

/**
 * Provides access to a pair of JAR entries with equal names in different JAR
 * files.
 *
 * @author Christian Schlichtherle
 */
public final class PairOfEntriesInFiles {

    private final EntryInFile entryInFile1, entryInFile2;

    public PairOfEntriesInFiles(
            final EntryInFile entryInFile1,
            final EntryInFile entryInFile2) {
        this.entryInFile1 = requireNonNull(entryInFile1);
        this.entryInFile2 = requireNonNull(entryInFile2);
    }

    /** Returns the JAR entry in the first JAR file. */
    public EntryInFile entryInFile1() { return entryInFile1; }

    /** Returns the JAR entry in the second JAR file. */
    public EntryInFile entryInFile2() { return entryInFile2; }
}

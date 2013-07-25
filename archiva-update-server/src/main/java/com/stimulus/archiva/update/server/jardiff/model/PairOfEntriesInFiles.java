/*
 * Copyright (C) 2005-2013 Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.server.jardiff.model;

/**
 * A pair of JAR entries with equal names in different JAR files.
 *
 * @author Christian Schlichtherle
 */
public interface PairOfEntriesInFiles {
    /** Returns the JAR entry in the first JAR file. */
    EntryInFile entryInFile1();

    /** Returns the JAR entry in the second JAR file. */
    EntryInFile entryInFile2();
}

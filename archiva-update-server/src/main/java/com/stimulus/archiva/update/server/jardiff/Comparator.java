/*
 * Copyright (C) 2005-2013 Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.server.jardiff;

/**
 * Compares two JAR entries in different JAR files.
 *
 * @author Christian Schlichtherle
 */
public interface Comparator {
    /**
     * Returns {@code true} if and only if the two given JAR entries in
     * different JAR files should be considered to be equal.
     *
     * @param entryInFile1 the JAR entry in the first JAR file.
     * @param entryInFile2 the JAR entry in the second JAR file.
     */
    boolean equals(EntryInFile entryInFile1, EntryInFile entryInFile2);
}

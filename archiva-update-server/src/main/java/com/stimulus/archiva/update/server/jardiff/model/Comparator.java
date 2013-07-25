/*
 * Copyright (C) 2005-2013 Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.server.jardiff.model;

import java.io.IOException;

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
     * @throws IOException at the discretion of the implementation, e.g. if
     *         it's impossible to read the entry contents for some reason.
     */
    boolean equals(EntryInFile entryInFile1, EntryInFile entryInFile2)
    throws IOException;
}

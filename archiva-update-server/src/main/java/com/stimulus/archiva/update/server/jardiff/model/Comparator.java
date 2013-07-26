/*
 * Copyright (C) 2005-2013 Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.server.jardiff.model;

/**
 * Compares two JAR entries in different JAR files.
 *
 * @param <X> the type of the exceptions to be thrown from the
 *        {@link #equals(EntryInFile, EntryInFile)} method.
 * @see Visitor
 * @author Christian Schlichtherle
 */
public interface Comparator<X extends Exception> {
    /**
     * Returns {@code true} if and only if the two given JAR entries in
     * different JAR files should be considered to be equal.
     *
     * @param entryInFile1 the JAR entry in the first JAR file.
     * @param entryInFile2 the JAR entry in the second JAR file.
     * @throws X at the discretion of the implementation.
     */
    boolean equals(EntryInFile entryInFile1, EntryInFile entryInFile2)
    throws X;
}

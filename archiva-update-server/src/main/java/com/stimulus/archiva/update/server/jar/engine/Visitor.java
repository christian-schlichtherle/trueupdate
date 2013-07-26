/*
 * Copyright (C) 2005-2013 Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.server.jar.engine;

/**
 * A visitor of two JAR files.
 * Note that the order of the calls to the visitor methods is currently
 * undefined, so you should not depend on the behavior of a particular
 * implementation in order to ensure compatibility with future versions.
 *
 * @param <X> the type of the exceptions to be thrown from the visitor methods.
 * @author Christian Schlichtherle
 */
public interface Visitor<X extends Exception> {
    /**
     * Visits a JAR entry which is present in the first JAR file,
     * but not in the second JAR file.
     *
     * @param entryInFile1 the JAR entry in the first JAR file.
     * @throws X at the discretion of the implementation.
     */
    abstract void visitEntryInFile1(EntryInFile entryInFile1) throws X;

    /**
     * Visits a JAR entry which is present in the second JAR file,
     * but not in the first JAR file.
     *
     * @param entryInFile2 the JAR entry in the second JAR file.
     * @throws X at the discretion of the implementation.
     */
    abstract void visitEntryInFile2(EntryInFile entryInFile2) throws X;

    /**
     * Visits a pair of JAR entries with equal names in the first and
     * second JAR file.
     *
     * @param entryInFile1 the JAR entry in the first JAR file.
     * @param entryInFile2 the JAR entry in the second JAR file.
     * @throws X at the discretion of the implementation.
     */
    abstract void visitEntriesInFiles(EntryInFile entryInFile1,
                                      EntryInFile entryInFile2)
    throws X;
}

/*
 * Copyright (C) 2005-2013 Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.server.jardiff.model;

/**
 * A visitor of a JAR diff.
 * Note that the order of the calls to the visitor methods is currently
 * undefined, so you should not depend on the behavior of a particular
 * implementation in order to ensure compatibility with future versions.
 *
 * @author Christian Schlichtherle
 */
public interface Visitor<X extends Exception> {
    /**
     * Visits a JAR entry which is present in the first JAR file,
     * but not in the second JAR file.
     *
     * @param entryInFile1 the JAR entry in the first JAR file.
     * @throws Exception at the discretion of the implementation.
     */
    abstract void visitEntryInFile1(EntryInFile entryInFile1) throws X;

    /**
     * Visits a JAR entry which is present in the second JAR file,
     * but not in the first JAR file.
     *
     * @param entryInFile2 the JAR entry in the second JAR file.
     * @throws Exception at the discretion of the implementation.
     */
    abstract void visitEntryInFile2(EntryInFile entryInFile2) throws X;

    /**
     * Visits a pair of JAR entries with an equal name in the first and
     * second JAR file and which are considered to be equal according to some
     * {@link Comparator}.
     *
     * @param entryInFile1 the JAR entry in the first JAR file.
     * @param entryInFile2 the JAR entry in the second JAR file.
     * @throws Exception at the discretion of the implementation.
     */
    abstract void visitEqualEntries(EntryInFile entryInFile1,
                                    EntryInFile entryInFile2)
    throws X;

    /**
     * Visits a pair of JAR entries with an equal name in the first and
     * second JAR file, but which are considered to be different according to
     * some {@link Comparator}.
     *
     * @param entryInFile1 the JAR entry in the first JAR file.
     * @param entryInFile2 the JAR entry in the second JAR file.
     * @throws Exception at the discretion of the implementation.
     */
    abstract void visitDifferentEntries(EntryInFile entryInFile1,
                                        EntryInFile entryInFile2)
    throws X;
}

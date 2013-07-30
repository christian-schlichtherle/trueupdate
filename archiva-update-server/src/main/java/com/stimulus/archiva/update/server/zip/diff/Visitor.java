/*
 * Copyright (C) 2005-2013 Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.server.zip.diff;

import com.stimulus.archiva.update.core.zip.commons.EntrySource;

/**
 * A visitor of two ZIP files.
 * Note that the order of the calls to the visitor methods is currently
 * undefined, so you should not depend on the behavior of a particular
 * implementation in order to ensure compatibility with future versions.
 *
 * @param <X> the type of the exceptions to be thrown from the visitor methods.
 * @author Christian Schlichtherle
 */
interface Visitor<X extends Exception> {
    /**
     * Visits a ZIP entry which is present in the first ZIP file,
     * but not in the second ZIP file.
     *
     * @param entrySource1 the ZIP entry in the first ZIP file.
     * @throws X at the discretion of the implementation.
     */
    void visitEntryInFirstFile(EntrySource entrySource1) throws X;

    /**
     * Visits a ZIP entry which is present in the second ZIP file,
     * but not in the first ZIP file.
     *
     * @param entrySource2 the ZIP entry in the second ZIP file.
     * @throws X at the discretion of the implementation.
     */
    void visitEntryInSecondFile(EntrySource entrySource2) throws X;

    /**
     * Visits a pair of ZIP entries with equal names in the first and
     * second ZIP file.
     *
     * @param firstEntrySource the ZIP entry in the first ZIP file.
     * @param secondEntrySource the ZIP entry in the second ZIP file.
     * @throws X at the discretion of the implementation.
     */
    void visitEntriesInBothFiles(EntrySource firstEntrySource,
                                 EntrySource secondEntrySource)
    throws X;
}

/*
 * Copyright (C) 2005-2013 Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.server.jar.diff;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import static java.util.Objects.requireNonNull;
import java.util.jar.*;
import javax.annotation.WillNotClose;
import javax.annotation.concurrent.Immutable;

/**
 * Lets a {@link Visitor} visit entries in two JAR files.
 *
 * @author Christian Schlichtherle
 */
@Immutable
final class Engine {

    private final @WillNotClose JarFile file1, file2;

    /**
     * Constructs an engine which operates on the given JAR files.
     *
     * @param file1 the first JAR file.
     * @param file2 the second JAR file.
     */
    Engine(final @WillNotClose JarFile file1,
           final @WillNotClose JarFile file2) {
        this.file1 = requireNonNull(file1);
        this.file2 = requireNonNull(file2);
    }

    /**
     * Processes the two JAR files and calls the visitor methods where
     * appropriate.
     *
     * @param visitor the visitor for the JAR entries in the JAR files.
     * @throws X at the discretion of the {@link Visitor}, in which case the
     *         engine aborts the visit.
     */
    <X extends Exception> void accept(final Visitor<X> visitor) throws X {
        for (final Enumeration<JarEntry> e1 = file1.entries();
             e1.hasMoreElements(); ) {
            final JarEntry entry1 = e1.nextElement();
            final JarEntry entry2 = file2.getJarEntry(entry1.getName());
            final EntryInFile entryInFile1 = entryInFile(entry1, file1);
            if (null == entry2)
                visitor.visitEntryInFile1(entryInFile1);
            else
                visitor.visitEntriesInFiles(entryInFile1,
                        entryInFile(entry2, file2));
        }

        for (final Enumeration<JarEntry> e2 = file2.entries();
             e2.hasMoreElements(); ) {
            final JarEntry entry2 = e2.nextElement();
            final JarEntry entry1 = file1.getJarEntry(entry2.getName());
            if (null == entry1)
                visitor.visitEntryInFile2(entryInFile(entry2, file2));
        }
    }

    /**
     * Constructs a JAR entry in a JAR file.
     *
     * @param entry the JAR entry.
     *              Note that this gets shared with this object.
     * @param file the JAR file.
     */
    private static EntryInFile entryInFile(
            final JarEntry entry,
            final JarFile file) {
        assert null != entry;
        assert null != file;
        return new EntryInFile() {
            @Override public JarEntry entry() { return entry; }
            @Override public InputStream input() throws IOException {
                return file.getInputStream(entry);
            }
        };
    }
}

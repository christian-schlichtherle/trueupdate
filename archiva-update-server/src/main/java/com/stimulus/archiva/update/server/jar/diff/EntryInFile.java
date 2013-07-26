/*
 * Copyright (C) 2005-2013 Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.server.jar.diff;

import com.stimulus.archiva.update.core.io.Source;
import java.io.IOException;
import java.io.InputStream;
import static java.util.Objects.requireNonNull;
import java.util.jar.*;

/**
 * Provides access to a JAR entry in a JAR file.
 *
 * @author Christian Schlichtherle
 */
final class EntryInFile implements Source {

    private final JarEntry entry;
    private final JarFile file;

    /**
     * Constructs a JAR entry in a JAR file.
     *
     * @param entry the JAR entry.
     *              Note that this gets shared with this object.
     * @param file the JAR file.
     */
    public EntryInFile(final JarEntry entry, final JarFile file) {
        this.entry = requireNonNull(entry);
        this.file = requireNonNull(file);
    }

    /**
     * Returns the JAR entry.
     * Clients should not modify the returned entry.
     */
    public JarEntry entry() { return entry; }

    /** Returns an input stream for reading the JAR entry contents. */
    @Override public InputStream input() throws IOException {
        return file.getInputStream(entry);
    }
}

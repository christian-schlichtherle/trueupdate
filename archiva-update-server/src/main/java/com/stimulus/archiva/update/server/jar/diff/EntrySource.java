/*
 * Copyright (C) 2005-2013 Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.server.jar.diff;

import com.stimulus.archiva.update.core.io.Source;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.*;

/**
 * Provides access to a JAR entry in a JAR file.
 *
 * @author Christian Schlichtherle
 */
final class EntrySource implements Source {

    private final JarEntry entry;
    private final JarFile file;

    EntrySource(final JarEntry entry, final JarFile file) {
        assert null != entry;
        this.entry = entry;
        assert null != file;
        this.file = file;
    }

    /** Returns the entry name. */
    String name() { return entry.getName(); }

    /** Returns an input stream for reading the JAR entry contents. */
    @Override public InputStream input() throws IOException {
        return file.getInputStream(entry);
    }
}

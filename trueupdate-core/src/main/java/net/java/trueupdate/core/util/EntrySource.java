/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.core.util;

import net.java.trueupdate.core.io.Source;
import java.io.*;
import static java.util.Objects.requireNonNull;
import java.util.zip.*;

/**
 * Provides access to a ZIP entry in a ZIP file.
 *
 * @author Christian Schlichtherle
 */
public final class EntrySource implements Source {

    private final ZipEntry entry;
    private final ZipFile file;

    public EntrySource(final ZipEntry entry, final ZipFile file) {
        this.entry = requireNonNull(entry);
        this.file = requireNonNull(file);
    }

    /** Returns the entry name. */
    public String name() { return entry.getName(); }

    /** Returns an input stream for reading the ZIP entry contents. */
    @Override public InputStream input() throws IOException {
        return file.getInputStream(entry);
    }
}

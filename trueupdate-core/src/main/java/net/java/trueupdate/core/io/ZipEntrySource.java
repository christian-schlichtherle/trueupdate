/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.core.io;

import java.io.*;
import static java.util.Objects.requireNonNull;
import java.util.zip.*;

/**
 * Reads a ZIP entry from a ZIP file.
 *
 * @see EntrySink
 * @author Christian Schlichtherle
 */
public final class ZipEntrySource implements Source {

    private final ZipEntry entry;
    private final ZipFile file;

    public ZipEntrySource(final ZipEntry entry, final ZipFile file) {
        this.entry = requireNonNull(entry);
        this.file = requireNonNull(file);
    }

    /** Returns the entry name. */
    public String name() { return entry.getName(); }

    /** Returns {@code true} if the entry is a directory entry. */
    public boolean directory() { return entry.isDirectory(); }

    /** Returns an input stream for reading the ZIP entry contents. */
    @Override public InputStream input() throws IOException {
        return file.getInputStream(entry);
    }
}

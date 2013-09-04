/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.core.zip;

import net.java.trueupdate.core.io.Source;

import java.io.*;
import java.util.zip.*;
import static net.java.trueupdate.shed.Objects.requireNonNull;

/**
 * Reads a ZIP entry from a ZIP file.
 *
 * @see ZipEntrySink
 * @author Christian Schlichtherle
 */
public final class ZipEntrySource implements Source {

    private final ZipEntry entry;
    private final ZipFile in;

    public ZipEntrySource(final ZipEntry entry, final ZipFile in) {
        this.entry = requireNonNull(entry);
        this.in = requireNonNull(in);
    }

    /** Returns the entry name. */
    public String name() { return entry.getName(); }

    /** Returns {@code true} if the entry is a directory entry. */
    public boolean directory() { return entry.isDirectory(); }

    /** Returns an input stream for reading the ZIP entry contents. */
    @Override public InputStream input() throws IOException {
        return in.getInputStream(entry);
    }
}

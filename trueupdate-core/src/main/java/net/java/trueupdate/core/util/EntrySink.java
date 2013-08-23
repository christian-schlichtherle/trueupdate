/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.core.util;

import java.io.*;
import static java.util.Objects.requireNonNull;
import java.util.zip.*;
import net.java.trueupdate.core.io.Sink;

/**
 * Writes a ZIP entry to a ZIP output stream.
 *
 * @author Christian Schlichtherle
 */
public class EntrySink implements Sink {

    private final ZipEntry entry;
    private final ZipOutputStream out;

    public EntrySink(final ZipEntry entry, final ZipOutputStream out) {
        this.entry = requireNonNull(entry);
        this.out = requireNonNull(out);
    }

    /** Returns the entry name. */
    public String name() { return entry.getName(); }

    @Override public OutputStream output() throws IOException {
        out.putNextEntry(entry);
        return new FilterOutputStream(out) {
            @Override public void close() throws IOException {
                ((ZipOutputStream) out).closeEntry();
            }
        };
    }
}

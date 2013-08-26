/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.core.io;

import java.io.*;
import java.util.zip.*;
import static net.java.trueupdate.shed.Objects.requireNonNull;

/**
 * Writes a ZIP entry to a ZIP output stream.
 *
 * @see ZipEntrySource
 * @author Christian Schlichtherle
 */
public final class ZipEntrySink implements Sink {

    private final ZipEntry entry;
    private final ZipOutputStream out;

    public ZipEntrySink(final ZipEntry entry, final ZipOutputStream out) {
        this.entry = requireNonNull(entry);
        this.out = requireNonNull(out);
    }

    /** Returns the entry name. */
    public String name() { return entry.getName(); }

    /** Returns {@code true} if the entry is a directory entry. */
    public boolean directory() { return entry.isDirectory(); }

    @Override public OutputStream output() throws IOException {
        if (directory()) {
            entry.setMethod(ZipOutputStream.STORED);
            entry.setSize(0);
            entry.setCompressedSize(0);
            entry.setCrc(0);
        }
        out.putNextEntry(entry);
        return new FilterOutputStream(out) {
            @Override public void close() throws IOException {
                ((ZipOutputStream) out).closeEntry();
            }
        };
    }
}

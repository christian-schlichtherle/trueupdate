/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.core.zip.io;

import java.io.*;
import java.util.zip.*;
import javax.annotation.WillCloseWhenClosed;
import net.java.trueupdate.util.Objects;

/**
 * Adapts a {@link ZipOutputStream} to a {@link ZipOutput}.
 *
 * @see ZipFileAdapter
 * @author Christian Schlichtherle
 */
public class ZipOutputStreamAdapter implements ZipOutput {

    /** The adapted ZIP output stream. */
    protected ZipOutputStream zip;

    /** Use of this constructor requires setting the {@code zip} field. */
    protected ZipOutputStreamAdapter() { }

    /**
     * Constructs a new ZIP output stream adapter for the given ZIP output
     * stream.
     */
    public ZipOutputStreamAdapter(final @WillCloseWhenClosed ZipOutputStream zip) {
        this.zip = Objects.requireNonNull(zip);
    }

    @Override public ZipEntry entry(String name) { return new ZipEntry(name); }

    @Override
    public OutputStream stream(final ZipEntry entry) throws IOException {
        zip.putNextEntry(entry);
        return new FilterOutputStream(zip) {
            @Override public void close() throws IOException {
                ((ZipOutputStream) out).closeEntry();
            }
        };
    }

    @Override public void close() throws IOException { zip.close(); }
}

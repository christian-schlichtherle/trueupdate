/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.core.zip;

import java.io.*;
import java.util.zip.*;
import javax.annotation.WillCloseWhenClosed;
import net.java.trueupdate.shed.Objects;

/**
 * Adapts a {@link ZipOutputStream} to a {@link ZipOutput}.
 *
 * @author Christian Schlichtherle
 */
public class ZipOutputStreamAdapter implements ZipOutput {

    private final ZipOutputStream zip;

    public ZipOutputStreamAdapter(final @WillCloseWhenClosed ZipOutputStream zip) {
        this.zip = Objects.requireNonNull(zip);
    }

    @Override public ZipEntry entry(String name) { return new ZipEntry(name); }

    @Override public OutputStream output(ZipEntry entry) throws IOException {
        zip.putNextEntry(entry);
        return new FilterOutputStream(zip) {
            @Override public void close() throws IOException {
                ((ZipOutputStream) out).closeEntry();
            }
        };
    }

    @Override public void close() throws IOException { zip.close(); }
}

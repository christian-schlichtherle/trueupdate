/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.core.zip;

import java.io.*;
import java.util.*;
import java.util.zip.*;
import javax.annotation.*;
import net.java.trueupdate.shed.Objects;

/**
 * Adapts a {@link ZipFile} to a {@link ZipInput}.
 *
 * @author Christian Schlichtherle
 */
public class ZipFileAdapter implements ZipInput {

    private final ZipFile zip;

    public ZipFileAdapter(final @WillCloseWhenClosed ZipFile input) {
        this.zip = Objects.requireNonNull(input);
    }

    @Override public Iterator<ZipEntry> iterator() {
        return new Iterator<ZipEntry>() {
            final Enumeration<? extends ZipEntry> en = zip.entries();

            @Override public boolean hasNext() { return en.hasMoreElements(); }

            @Override public ZipEntry next() { return en.nextElement(); }

            @Override public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Override public @Nullable ZipEntry entry(String name) {
        return zip.getEntry(name);
    }

    @Override public InputStream input(ZipEntry entry) throws IOException {
        return zip.getInputStream(entry);
    }

    @Override public void close() throws IOException { zip.close(); }
}

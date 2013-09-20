/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.core.zip.io;

import java.io.*;
import java.util.zip.*;
import net.java.trueupdate.util.Objects;

/**
 * A file based ZIP store.
 *
 * @author Christian Schlichtherle
 */
public class ZipFileStore implements ZipStore {

    final File file;

    public ZipFileStore(final File file) {
        this.file = Objects.requireNonNull(file);
    }

    @Override public ZipInput input() throws IOException {
        return new ZipFileAdapter(new ZipFile(file));
    }

    @Override public ZipOutput output() throws IOException {
        return new ZipOutputStreamAdapter(new ZipOutputStream(
                new FileOutputStream(file)));
    }

    @Override public void delete() throws IOException {
        if (!file.delete())
            throw new FileNotFoundException(file + " (cannot delete)");
    }

    @Override public boolean exists() { return file.exists(); }
}

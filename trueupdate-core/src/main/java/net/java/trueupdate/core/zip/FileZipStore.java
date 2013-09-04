/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.core.zip;

import net.java.trueupdate.shed.Objects;

import java.io.*;
import java.util.zip.*;

/**
 * A file based ZIP store.
 *
 * @author Christian Schlichtherle
 */
public class FileZipStore implements ZipStore {

    private final File file;

    public FileZipStore(final File file) {
        this.file = Objects.requireNonNull(file);
    }

    @Override public ZipFile input() throws IOException {
        return new ZipFile(file);
    }

    @Override public ZipOutputStream output() throws IOException {
        return new ZipOutputStream(new FileOutputStream(file));
    }

    @Override public void delete() throws IOException {
        if (!file.delete())
            throw new FileNotFoundException(file + " (cannot delete)");
    }

    @Override public boolean exists() { return file.exists(); }
}

/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.installer.core.tx;

import java.io.*;
import static net.java.trueupdate.installer.core.io.Files.*;

import net.java.trueupdate.core.zip.io.ZipFileStore;
import net.java.trueupdate.core.zip.io.ZipSource;
import net.java.trueupdate.manager.spec.tx.Transaction;

import static net.java.trueupdate.util.Objects.requireNonNull;

/**
 * A transaction which unzips a ZIP file to a directory.
 * An existing destination file or directory is <em>not</em> overwritten.
 *
 * @author Christian Schlichtherle
 */
public final class UnzipTransaction extends Transaction {

    private final ZipSource source;
    private final File directory;

    public UnzipTransaction(File zipFile, File directory) {
        this(new ZipFileStore(zipFile), directory);
    }

    public UnzipTransaction(final ZipSource source, final File directory) {
        this.source = requireNonNull(source);
        this.directory = requireNonNull(directory);
    }

    @Override public void prepare() throws Exception {
        if (directory.exists())
            throw new IOException(String.format(
                    "Will not overwrite existing file or directory %s .",
                    directory));
    }

    @Override public void perform() throws Exception {
        unzip(source, directory);
    }

    @Override public void rollback() throws IOException {
        deletePath(directory);
    }
}

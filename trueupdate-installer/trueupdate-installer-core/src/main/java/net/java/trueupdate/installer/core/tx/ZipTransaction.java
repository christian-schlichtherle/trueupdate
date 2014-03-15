/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.installer.core.tx;

import java.io.File;
import java.io.IOException;
import net.java.trueupdate.core.zip.io.ZipFileStore;
import net.java.trueupdate.core.zip.io.ZipStore;
import static net.java.trueupdate.installer.core.io.Files.zip;

import net.java.trueupdate.installer.core.io.Files;
import net.java.trueupdate.manager.spec.tx.Transaction;
import static net.java.trueupdate.util.Objects.requireNonNull;

/**
 * A transaction which zips a file or directory to a ZIP file.
 * An existing destination file or directory is <em>not</em> overwritten.
 *
 * @author Christian Schlichtherle
 */
public final class ZipTransaction extends Transaction {

    private final ZipStore store;
    private final File fileOrDirectory;
    private final String entryName;

    public ZipTransaction(File zipFile,
                          File fileOrDirectory,
                          String entryName) {
        this(new ZipFileStore(zipFile), fileOrDirectory, entryName);
    }

    public ZipTransaction(final ZipStore store,
                          final File fileOrDirectory,
                          final String entryName) {
        this.store = requireNonNull(store);
        this.fileOrDirectory = requireNonNull(fileOrDirectory);
        this.entryName = requireNonNull(entryName);
    }

    @Override public void prepare() throws IOException {
        if (store.exists())
            throw new IOException(String.format(
                    "Will not overwrite existing ZIP file or directory %s .",
                    store));
    }

    @Override public void perform() throws IOException {
        zip(store, fileOrDirectory, entryName);
    }

    @Override public void rollback() throws IOException {
        store.delete();
    }
}

/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.installer.core.cmd;

import net.java.trueupdate.core.zip.io.ZipFileStore;
import net.java.trueupdate.core.zip.io.ZipStore;
import net.java.trueupdate.manager.spec.cmd.AbstractCommand;

import java.io.File;
import java.io.IOException;

import static java.util.Objects.requireNonNull;
import static net.java.trueupdate.installer.core.io.Files.zip;

/**
 * A command which zips a file or directory to a ZIP file.
 * An existing destination file or directory is <em>not</em> overwritten.
 *
 * @author Christian Schlichtherle
 */
final public class ZipCommand extends AbstractCommand {

    private final ZipStore store;
    private final File fileOrDirectory;
    private final String entryName;

    public ZipCommand(File zipFile,
                      File fileOrDirectory,
                      String entryName) {
        this(new ZipFileStore(zipFile), fileOrDirectory, entryName);
    }

    public ZipCommand(final ZipStore store,
                      final File fileOrDirectory,
                      final String entryName) {
        this.store = requireNonNull(store);
        this.fileOrDirectory = requireNonNull(fileOrDirectory);
        this.entryName = requireNonNull(entryName);
    }

    @Override protected void doStart() throws IOException {
        if (store.exists())
            throw new IOException(String.format(
                    "Will not overwrite existing ZIP file or directory %s .",
                    store));
    }

    @Override protected void doPerform() throws IOException {
        zip(store, fileOrDirectory, entryName);
    }

    @Override protected void doRevert() throws IOException {
        store.delete();
    }
}

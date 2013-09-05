/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.core.tx;

import java.io.File;
import java.io.IOException;
import static net.java.trueupdate.manager.core.io.Files.*;
import static net.java.trueupdate.shed.Objects.requireNonNull;

/**
 * A transaction which zips a file or directory to a ZIP file.
 * An existing destination file or directory is <em>not</em> overwritten.
 *
 * @author Christian Schlichtherle
 */
public final class ZipTransaction extends Transaction {

    private final File zipFile, fileOrDirectory;
    private final String entryName;

    public ZipTransaction(final File zipFile,
                          final File fileOrDirectory,
                          final String entryName) {
        this.zipFile = requireNonNull(zipFile);
        this.fileOrDirectory = requireNonNull(fileOrDirectory);
        this.entryName = requireNonNull(entryName);
    }

    @Override protected void prepare() throws Exception {
        if (zipFile.exists())
            throw new IOException(String.format(
                    "Will not overwrite existing file or directory %s .",
                    zipFile));
    }

    @Override protected void perform() throws Exception {
        zip(zipFile, fileOrDirectory, entryName);
    }

    @Override protected void rollback() throws IOException {
        deletePath(zipFile);
    }
}

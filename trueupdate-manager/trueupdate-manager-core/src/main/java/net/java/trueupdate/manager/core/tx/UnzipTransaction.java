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
 * A transaction which unzips a ZIP file to a directory.
 * An existing destination file or directory is <em>not</em> overwritten.
 *
 * @author Christian Schlichtherle
 */
public final class UnzipTransaction extends Transaction {

    private final File zipFile, directory;

    public UnzipTransaction(final File zipFile, final File directory) {
        this.zipFile = requireNonNull(zipFile);
        this.directory = requireNonNull(directory);
    }

    @Override protected void prepare() throws Exception {
        if (directory.exists())
            throw new IOException(String.format(
                    "Will not overwrite existing file or directory %s .",
                    directory));
    }

    @Override protected void perform() throws Exception {
        unzip(zipFile, directory);
    }

    @Override protected void rollback() throws IOException {
        deletePath(directory);
    }
}

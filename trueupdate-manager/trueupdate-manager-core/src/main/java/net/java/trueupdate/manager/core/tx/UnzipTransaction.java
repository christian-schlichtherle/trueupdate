/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.core.tx;

import java.io.*;
import static net.java.trueupdate.manager.core.io.Files.*;
import net.java.trueupdate.core.zip.*;
import static net.java.trueupdate.shed.Objects.requireNonNull;

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

    @Override protected void prepare() throws Exception {
        if (directory.exists())
            throw new IOException(String.format(
                    "Will not overwrite existing file or directory %s .",
                    directory));
    }

    @Override protected void perform() throws Exception {
        unzip(source, directory);
    }

    @Override protected void rollback() throws IOException {
        deletePath(directory);
    }
}

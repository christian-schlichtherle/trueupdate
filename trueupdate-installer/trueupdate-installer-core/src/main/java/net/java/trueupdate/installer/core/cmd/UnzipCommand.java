/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.installer.core.cmd;

import net.java.trueupdate.core.zip.io.ZipFileStore;
import net.java.trueupdate.core.zip.io.ZipSource;
import net.java.trueupdate.manager.spec.cmd.AbstractCommand;

import java.io.File;
import java.io.IOException;

import static net.java.trueupdate.installer.core.io.Files.deletePath;
import static net.java.trueupdate.installer.core.io.Files.unzip;
import static net.java.trueupdate.util.Objects.requireNonNull;

/**
 * A command which unzips a ZIP file to a directory.
 * An existing destination file or directory is <em>not</em> overwritten.
 *
 * @author Christian Schlichtherle
 */
final public class UnzipCommand extends AbstractCommand {

    private final ZipSource source;
    private final File directory;

    public UnzipCommand(File zipFile, File directory) {
        this(new ZipFileStore(zipFile), directory);
    }

    public UnzipCommand(final ZipSource source, final File directory) {
        this.source = requireNonNull(source);
        this.directory = requireNonNull(directory);
    }

    @Override protected void doStart() throws IOException {
        if (directory.exists())
            throw new IOException(String.format(
                    "Will not overwrite existing file or directory %s .",
                    directory));
    }

    @Override protected void doPerform() throws IOException {
        unzip(source, directory);
    }

    @Override protected void doRevert() throws IOException {
        deletePath(directory);
    }
}

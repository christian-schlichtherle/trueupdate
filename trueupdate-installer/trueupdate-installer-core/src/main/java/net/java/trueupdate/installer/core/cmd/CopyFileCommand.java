/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.installer.core.cmd;

import net.java.trueupdate.manager.spec.cmd.AbstractCommand;

import java.io.File;
import java.io.IOException;

import static java.util.Objects.requireNonNull;
import static net.java.trueupdate.installer.core.io.Files.copyFile;
import static net.java.trueupdate.installer.core.io.Files.deletePath;

/**
 * A command which copies a file.
 * An existing destination file or directory is <em>not</em> overwritten.
 *
 * @author Christian Schlichtherle
 */
final public class CopyFileCommand extends AbstractCommand {

    private final File from, to;

    public CopyFileCommand(final File from, final File to) {
        this.from = requireNonNull(from);
        this.to = requireNonNull(to);
    }

    @Override protected void doStart() throws IOException {
        if (to.exists())
            throw new IOException(String.format(
                    "Will not overwrite existing file or directory %s .",
                    to));
    }

    @Override protected void doPerform() throws IOException {
        copyFile(from, to);
    }

    @Override protected void doRevert() throws IOException {
        deletePath(to);
    }
}

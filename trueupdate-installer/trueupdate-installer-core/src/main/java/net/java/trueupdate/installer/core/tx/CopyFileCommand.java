/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.installer.core.tx;

import java.io.File;
import java.io.IOException;
import static net.java.trueupdate.installer.core.io.Files.copyFile;
import static net.java.trueupdate.installer.core.io.Files.deletePath;

import net.java.trueupdate.manager.spec.tx.AbstractCommand;
import net.java.trueupdate.manager.spec.tx.Command;
import static net.java.trueupdate.util.Objects.requireNonNull;

/**
 * A copy file command.
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

    @Override protected void onStart() throws IOException {
        if (to.exists())
            throw new IOException(String.format(
                    "Will not overwrite existing file or directory %s .",
                    to));
    }

    @Override protected void onPerform() throws IOException {
        copyFile(from, to);
    }

    @Override protected void onRevert() throws IOException {
        deletePath(to);
    }
}

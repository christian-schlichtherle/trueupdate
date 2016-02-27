/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.installer.core.cmd;

import net.java.trueupdate.manager.spec.cmd.Command;

import java.io.File;
import java.io.IOException;

import static java.util.Objects.requireNonNull;
import static net.java.trueupdate.installer.core.io.Files.renamePath;

/**
 * A command which renames a path.
 * The source path may refer to a file or directory.
 * An existing destination file or directory is <em>not</em> overwritten.
 *
 * @author Christian Schlichtherle
 */
final public class RenamePathCommand implements Command {

    private final File from, to;

    public RenamePathCommand(final File from, final File to) {
        this.from = requireNonNull(from);
        this.to = requireNonNull(to);
    }

    @Override public void perform() throws IOException {
        renamePath(from, to);
    }

    @Override public void revert() throws IOException {
        if (to.exists()) renamePath(to, from);
    }
}

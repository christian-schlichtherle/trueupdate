/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.installer.core.tx;

import java.io.File;
import java.io.IOException;
import static net.java.trueupdate.installer.core.io.Files.deletePath;

import net.java.trueupdate.manager.spec.tx.AbstractCommand;
import net.java.trueupdate.manager.spec.tx.Command;
import static net.java.trueupdate.util.Objects.requireNonNull;

/**
 * A transaction which monitors the side effect of a {@link PathTask} on a file
 * or directory.
 * Before the task is called, the file or directory must not exist.
 * After the task has successfully terminated, the file or directory must exist.
 *
 * @author Christian Schlichtherle
 */
final public class PathTaskCommand extends AbstractCommand {

    private final File path;
    private final PathTask<?, ?> task;

    public PathTaskCommand(final File path, final PathTask<?, ?> task) {
        this.path = requireNonNull(path);
        this.task = requireNonNull(task);
    }

    @Override protected void onStart() throws IOException {
        if (path.exists())
            throw new IOException(String.format(
                    "Will not overwrite existing file or directory %s .",
                    path));
    }

    @Override protected void onPerform() throws Exception {
        task.execute(path);
        if (!path.exists())
            throw new IOException(String.format(
                    "Path task did not create file or directory %s .",
                    path));
    }

    @Override protected void onRevert() throws IOException {
        deletePath(path);
    }
}

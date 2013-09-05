/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.core.tx;

import net.java.trueupdate.manager.core.io.PathTask;

import java.io.File;
import java.io.IOException;
import static net.java.trueupdate.manager.core.io.Files.*;
import static net.java.trueupdate.shed.Objects.requireNonNull;

/**
 * A transaction which monitors the side effect of a {@link PathTask} on a file
 * or directory.
 * Before the task is called, the file or directory must not exist.
 * After the task has successfully terminated, the file or directory must exist.
 *
 * @author Christian Schlichtherle
 */
public final class PathTaskTransaction extends Transaction {

    private final File path;
    private final PathTask<?, ?> task;

    public PathTaskTransaction(
            final File path,
            final PathTask<?, ?> task) {
        this.path = requireNonNull(path);
        this.task = requireNonNull(task);
    }

    @Override protected void prepare() throws Exception {
        if (path.exists())
            throw new IOException(String.format(
                    "Will not overwrite existing file or directory %s .",
                    path));
    }

    @Override protected void perform() throws Exception {
        task.execute(path);
        if (!path.exists())
            throw new IOException(String.format(
                    "Path task did not create file or directory %s .",
                    path));
    }

    @Override
    protected void rollback() throws IOException { deletePath(path); }
}

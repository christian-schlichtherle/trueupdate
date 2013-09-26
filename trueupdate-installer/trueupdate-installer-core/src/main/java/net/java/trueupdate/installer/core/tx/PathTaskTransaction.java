/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.installer.core.tx;

import java.io.File;
import java.io.IOException;
import static net.java.trueupdate.installer.core.io.Files.*;
import net.java.trueupdate.installer.core.io.PathTask;
import net.java.trueupdate.manager.spec.tx.Transaction;

import static net.java.trueupdate.util.Objects.requireNonNull;

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

    @Override public void prepare() throws Exception {
        if (path.exists())
            throw new IOException(String.format(
                    "Will not overwrite existing file or directory %s .",
                    path));
    }

    @Override public void perform() throws Exception {
        task.execute(path);
        if (!path.exists())
            throw new IOException(String.format(
                    "Path task did not create file or directory %s .",
                    path));
    }

    @Override
    public void rollback() throws IOException { deletePath(path); }
}

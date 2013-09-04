/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.core.tx;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;

import static net.java.trueupdate.manager.core.io.Files.*;
import static net.java.trueupdate.shed.Objects.requireNonNull;

/**
 * A transaction which monitors the side effect of a {@link Callable} (task) on
 * a single output file or directory (path).
 * Before the transaction starts, the output file or directory must not exist.
 *
 * @author Christian Schlichtherle
 */
public final class PathTaskTransaction extends Transaction {

    private final File path;
    private final Callable<?> task;

    public PathTaskTransaction(
            final File path,
            final Callable<?> task) {
        this.path = requireNonNull(path);
        this.task = requireNonNull(task);
    }

    @Override protected void prepare() throws Exception {
        if (path.exists())
            throw new IOException(String.format(
                    "Will not overwrite existing file or directory %s .",
                    path));
    }

    @Override protected void perform() throws Exception { task.call(); }

    @Override
    protected void rollback() throws IOException { deletePath(path); }
}

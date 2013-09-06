/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.core.io;

import java.io.*;
import net.java.trueupdate.shed.Objects;

/**
 * @see Sources#execute
 * @author Christian Schlichtherle
 */
final class WithInputTask<V, X extends Exception>
implements Sources.ExecuteStatement<V, X> {

    private final InputTask<V, X> task;

    WithInputTask(final InputTask<V, X> task) {
        this.task = Objects.requireNonNull(task);
    }

    @Override public V on(File file) throws X, IOException {
        return on(new FileStore(file));
    }

    @Override public V on(Source source) throws X, IOException {
        return Closeables.execute(task, source.input());
    }
}

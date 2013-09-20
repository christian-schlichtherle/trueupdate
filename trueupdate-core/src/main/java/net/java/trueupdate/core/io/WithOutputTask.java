/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.core.io;

import java.io.*;
import net.java.trueupdate.util.Objects;

/**
 * @see Sinks#execute
 * @author Christian Schlichtherle
 */
final class WithOutputTask<V, X extends Exception>
implements Sinks.ExecuteStatement<V, X> {

    private final OutputTask<V, X> task;

    WithOutputTask(final OutputTask<V, X> task) {
        this.task = Objects.requireNonNull(task);
    }

    @Override public V on(File file) throws X, IOException {
        return on(new FileStore(file));
    }

    @Override public V on(Sink sink) throws X, IOException {
        return Closeables.execute(task, sink.output());
    }
}

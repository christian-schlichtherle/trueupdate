/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.core.io;

import java.io.IOException;
import java.io.InputStream;
import net.java.trueupdate.shed.Objects;

/**
 * @see Sources#execute
 * @see Sources#bind
 * @author Christian Schlichtherle
 */
final class WithInputTask<V, X extends Exception>
implements Sources.BindStatement<V, X>, Sources.ExecuteStatement<V, X> {

    private final InputTask<V, X> task;

    WithInputTask(final InputTask<V, X> task) {
        this.task = Objects.requireNonNull(task);
    }

    @Override public Job<V, X> to(final Source source) {
        class WithTaskAndSourceJob implements Job<V, X> {
            @Override public V call() throws X, IOException {
                return on(source);
            }
        }
        return new WithTaskAndSourceJob();
    }

    @Override @SuppressWarnings("unchecked")
    public V on(Source source) throws X, IOException {
        return on(source.input());
    }

    @Override public V on(InputStream in) throws X, IOException {
        return Closeables.execute(task, in);
    }
}

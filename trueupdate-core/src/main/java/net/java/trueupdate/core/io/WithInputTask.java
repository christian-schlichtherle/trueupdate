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

    @Override public V on(final InputStream in) throws X, IOException {
        class OneTimeSource implements Source {
            @Override public InputStream input() {
                return in;
            }
        }
        return on(new OneTimeSource());
    }

    @Override @SuppressWarnings("unchecked")
    public V on(Source source) throws X, IOException {
        return Closeables.execute(task, source.input());
    }
}

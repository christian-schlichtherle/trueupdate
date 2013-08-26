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

    @Override public IoCallable<V, X> to(final Source source) {
        class WithTaskAndSource implements IoCallable<V, X> {
            @Override public V call() throws X, IOException {
                return on(source);
            }
        }
        return new WithTaskAndSource();
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
    public V on(final Source source) throws X, IOException {
        X ex = null;
        final InputStream in = source.input();
        try {
            return task.execute(in);
        } catch (Exception ex2) {
            throw ex = (X) ex2;
        } finally {
            try {
                in.close();
            } catch (IOException ex2) {
                if (null == ex) {
                    throw ex2;
                }
            }
        }
    }
}

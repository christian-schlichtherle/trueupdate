/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.core.io;

import java.io.IOException;
import java.io.OutputStream;
import net.java.trueupdate.shed.Objects;

/**
 * @see Sinks#execute
 * @see Sinks#bind
 * @author Christian Schlichtherle
 */
final class WithOutputTask<V, X extends Exception>
implements Sinks.BindStatement<V, X>, Sinks.ExecuteStatement<V, X> {

    private final OutputTask<V, X> task;

    WithOutputTask(final OutputTask<V, X> task) {
        this.task = Objects.requireNonNull(task);
    }

    @Override public IoCallable<V, X> to(final Sink sink) {
        class WithTaskAndSource implements IoCallable<V, X> {
            @Override public V call() throws X, IOException {
                return on(sink);
            }
        }
        return new WithTaskAndSource();
    }

    @Override public V on(final OutputStream out) throws X, IOException {
        class OneTimeSink implements Sink {
            @Override public OutputStream output() {
                return out;
            }
        }
        return on(new OneTimeSink());
    }

    @Override @SuppressWarnings("unchecked")
    public V on(final Sink sink) throws X, IOException {
        X ex = null;
        final OutputStream out = sink.output();
        try {
            return task.execute(out);
        } catch (Exception ex2) {
            throw ex = (X) ex2;
        } finally {
            try {
                out.close();
            } catch (IOException ex2) {
                if (null == ex) {
                    throw ex2;
                }
            }
        }
    }
}

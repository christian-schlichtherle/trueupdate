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

    @Override public Job<V, X> to(final Sink sink) {
        class WithTaskAndSourceJob implements Job<V, X> {
            @Override public V call() throws X, IOException {
                return on(sink);
            }
        }
        return new WithTaskAndSourceJob();
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
    public V on(Sink sink) throws X, IOException {
        return Closeables.execute(task, sink.output());
    }
}

/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.core.io;

import java.io.*;
import javax.annotation.WillClose;
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

    @Override public Job<V, X> to(File file) {
        return to(new FileStore(file));
    }

    @Override public Job<V, X> to(final Sink sink) {
        class WithTaskAndSourceJob implements Job<V, X> {
            @Override public V call() throws X, IOException {
                return on(sink);
            }
        }
        return new WithTaskAndSourceJob();
    }

    @Override public V on(File file) throws X, IOException {
        return on(new FileOutputStream(file));
    }

    @Override public V on(Sink sink) throws X, IOException {
        return on(sink.output());
    }

    @Override public V on(@WillClose OutputStream out) throws X, IOException {
        return Closeables.execute(task, out);
    }
}

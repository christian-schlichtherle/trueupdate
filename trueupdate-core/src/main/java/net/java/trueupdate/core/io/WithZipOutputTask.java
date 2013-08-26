/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.core.io;

import java.io.IOException;
import java.util.zip.ZipOutputStream;
import net.java.trueupdate.shed.Objects;

/**
 * @see Sinks#execute
 * @see Sinks#bind
 * @author Christian Schlichtherle
 */
final class WithZipOutputTask<V, X extends Exception>
implements ZipSinks.BindStatement<V, X>, ZipSinks.ExecuteStatement<V, X> {

    private final ZipOutputTask<V, X> task;

    WithZipOutputTask(final ZipOutputTask<V, X> task) {
        this.task = Objects.requireNonNull(task);
    }

    @Override public IoCallable<V, X> to(final ZipSink sink) {
        class WithTaskAndSource implements IoCallable<V, X> {
            @Override public V call() throws X, IOException {
                return on(sink);
            }
        }
        return new WithTaskAndSource();
    }

    @Override public V on(final ZipOutputStream zipOut) throws X, IOException {
        class OneTimeSink implements ZipSink {
            @Override public ZipOutputStream output() {
                return zipOut;
            }
        }
        return on(new OneTimeSink());
    }

    @Override @SuppressWarnings("unchecked")
    public V on(final ZipSink sink) throws X, IOException {
        X ex = null;
        final ZipOutputStream zipOut = sink.output();
        try {
            return task.execute(zipOut);
        } catch (Exception ex2) {
            throw ex = (X) ex2;
        } finally {
            try {
                zipOut.close();
            } catch (IOException ex2) {
                if (null == ex) {
                    throw ex2;
                }
            }
        }
    }
}

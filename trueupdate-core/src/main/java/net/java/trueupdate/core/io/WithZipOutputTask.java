/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.core.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipOutputStream;
import javax.annotation.WillClose;
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

    @Override public Job<V, X> to(final File file) {
        class WithTaskAndFileJob implements Job<V, X> {
            @Override public V call() throws X, IOException {
                return on(file);
            }
        }
        return new WithTaskAndFileJob();
    }

    @Override public Job<V, X> to(final ZipSink sink) {
        class WithTaskAndSourceJob implements Job<V, X> {
            @Override public V call() throws X, IOException {
                return on(sink);
            }
        }
        return new WithTaskAndSourceJob();
    }

    @Override public V on(File file) throws X, IOException {
        return on(new ZipOutputStream(new FileOutputStream(file)));
    }

    @Override public V on(ZipSink sink) throws X, IOException {
        return on(sink.output());
    }

    @Override
    public V on(@WillClose ZipOutputStream out) throws X, IOException {
        return Closeables.execute(task, out);
    }
}

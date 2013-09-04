/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.core.io;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipFile;
import javax.annotation.WillClose;
import net.java.trueupdate.shed.Objects;

/**
 * @see Sources#execute
 * @see Sources#bind
 * @author Christian Schlichtherle
 */
final class WithZipInputTask<V, X extends Exception>
implements ZipSources.BindStatement<V, X>, ZipSources.ExecuteStatement<V, X> {

    private final ZipInputTask<V, X> task;

    WithZipInputTask(final ZipInputTask<V, X> task) {
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

    @Override public Job<V, X> to(final ZipSource source) {
        class WithTaskAndSourceJob implements Job<V, X> {
            @Override public V call() throws X, IOException {
                return on(source);
            }
        }
        return new WithTaskAndSourceJob();
    }

    @Override public V on(File file) throws X, IOException {
        return on(new ZipFile(file));
    }

    @Override public V on(ZipSource source) throws X, IOException {
        return on(source.input());
    }

    @Override @SuppressWarnings("unchecked")
    public V on(final @WillClose ZipFile archive) throws X, IOException {
        // Unfortunately, in Java SE 6, ZipFile is not a Closeable.
        // In Java SE 7, the entire DSL is replaceable with the
        // try-with-resources statement.
        //return Closeables.execute(task, sink.output());
        X ex = null;
        try {
            return task.execute(archive);
        } catch (Exception ex2) {
            throw ex = (X) ex2;
        } finally {
            try {
                archive.close();
            } catch (IOException ex2) {
                if (null == ex) {
                    throw ex2;
                }
            }
        }
    }
}

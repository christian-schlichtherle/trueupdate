/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.core.io;

import java.io.IOException;
import java.util.zip.ZipFile;
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

    @Override public IoCallable<V, X> to(final ZipSource source) {
        class WithTaskAndSource implements IoCallable<V, X> {
            @Override public V call() throws X, IOException {
                return on(source);
            }
        }
        return new WithTaskAndSource();
    }

    @Override public V on(final ZipFile zipFile) throws X, IOException {
        class OneTimeSource implements ZipSource {
            @Override public ZipFile input() {
                return zipFile;
            }
        }
        return on(new OneTimeSource());
    }

    @Override @SuppressWarnings("unchecked")
    public V on(final ZipSource source) throws X, IOException {
        X ex = null;
        final ZipFile zipFile = source.input();
        try {
            return task.execute(zipFile);
        } catch (Exception ex2) {
            throw ex = (X) ex2;
        } finally {
            try {
                zipFile.close();
            } catch (IOException ex2) {
                if (null == ex) {
                    throw ex2;
                }
            }
        }
    }
}

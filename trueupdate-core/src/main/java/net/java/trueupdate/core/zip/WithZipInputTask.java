/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.core.zip;

import java.io.File;
import java.io.IOException;
import javax.annotation.WillClose;
import net.java.trueupdate.core.io.Closeables;
import net.java.trueupdate.core.io.Job;
import net.java.trueupdate.shed.Objects;

/**
 * @see ZipSources#bind
 * @see ZipSources#execute
 * @author Christian Schlichtherle
 */
final class WithZipInputTask<V, X extends Exception>
implements ZipSources.BindStatement<V, X>, ZipSources.ExecuteStatement<V, X> {

    private final ZipInputTask<V, X> task;

    WithZipInputTask(final ZipInputTask<V, X> task) {
        this.task = Objects.requireNonNull(task);
    }

    @Override public Job<V, X> to(File file) {
        return to(new ZipFileStore(file));
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
        return on(new ZipFileStore(file));
    }

    @Override public V on(ZipSource source) throws X, IOException {
        return Closeables.execute(task, source.input());
    }
}

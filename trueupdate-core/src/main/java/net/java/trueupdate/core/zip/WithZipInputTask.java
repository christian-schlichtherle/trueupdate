/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.core.zip;

import java.io.*;
import net.java.trueupdate.core.io.*;
import net.java.trueupdate.util.Objects;

/**
 * @see ZipSources#execute
 * @author Christian Schlichtherle
 */
final class WithZipInputTask<V, X extends Exception>
implements ZipSources.ExecuteStatement<V, X> {

    private final ZipInputTask<V, X> task;

    WithZipInputTask(final ZipInputTask<V, X> task) {
        this.task = Objects.requireNonNull(task);
    }

    @Override public V on(File file) throws X, IOException {
        return on(new ZipFileStore(file));
    }

    @Override public V on(ZipSource source) throws X, IOException {
        return Closeables.execute(task, source.input());
    }
}

/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.core.zip;

import java.io.*;
import net.java.trueupdate.core.io.Job;

/**
 * Provides functions for {@link ZipSource}s.
 *
 * @see ZipSinks
 * @author Christian Schlichtherle
 */
public class ZipSources {

    public static <V, X extends Exception>
            BindStatement<V, X> bind(ZipInputTask<V, X> task) {
        return new WithZipInputTask<V, X>(task);
    }

    public interface BindStatement<V, X extends Exception> {
        Job<V, X> to(File file);
        Job<V, X> to(ZipSource source);
    }

    public static <V, X extends Exception>
            ExecuteStatement<V, X> execute(ZipInputTask<V, X> task) {
        return new WithZipInputTask<V, X>(task);
    }

    public interface ExecuteStatement<V, X extends Exception> {
        V on(File file) throws X, IOException;
        V on(ZipSource source) throws X, IOException;
    }

    private ZipSources() { }
}

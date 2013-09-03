/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.core.io;

import java.io.IOException;
import java.util.zip.ZipOutputStream;
import javax.annotation.WillClose;

/**
 * Provides functions for {@link ZipSink}s.
 *
 * @author Christian Schlichtherle
 */
public class ZipSinks {

    public static <V, X extends Exception>
            ExecuteStatement<V, X> execute(ZipOutputTask<V, X> task) {
        return new WithZipOutputTask<V, X>(task);
    }

    public interface ExecuteStatement<V, X extends Exception> {
        V on(@WillClose ZipOutputStream out) throws X, IOException;
        V on(ZipSink sink) throws X, IOException;
    }

    public static <V, X extends Exception>
            BindStatement<V, X> bind(ZipOutputTask<V, X> task) {
        return new WithZipOutputTask<V, X>(task);
    }

    public interface BindStatement<V, X extends Exception> {
        Job<V, X> to(ZipSink sink);
    }

    private ZipSinks() { }
}

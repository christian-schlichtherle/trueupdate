/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.core.io;

import java.io.*;
import java.util.concurrent.Callable;
import net.java.trueupdate.shed.Objects;

/**
 * A poor man's substitute for Java SE 7' try-with-resources statement.
 *
 * @author Christian Schlichtherle
 */
public abstract class OutputTask<V, X extends Exception>
implements Callable<V> {

    private final Sink sink;

    public OutputTask(final Sink sink) {
        this.sink = Objects.requireNonNull(sink);
    }

    @SuppressWarnings("unchecked")
    @Override public final V call() throws X, IOException {
        X ex = null;
        final OutputStream out = sink.output();
        try {
            return execute(out);
        } catch (Exception ex2) {
            throw ex = (X) ex2;
        } finally {
            try {
                out.close();
            } catch (IOException ex2) {
                if (null == ex) throw ex2;
            }
        }
    }

    protected abstract V execute(OutputStream out) throws X;
}

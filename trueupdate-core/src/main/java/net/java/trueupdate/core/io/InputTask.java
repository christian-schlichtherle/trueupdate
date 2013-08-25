/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.core.io;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Callable;
import net.java.trueupdate.shed.Objects;

/**
 * A poor man's substitute for Java SE 7' try-with-resources statement.
 *
 * @author Christian Schlichtherle
 */
public abstract class InputTask<V, X extends Exception>
implements Callable<V> {

    private final Source source;

    public InputTask(final Source source) {
        this.source = Objects.requireNonNull(source);
    }

    @SuppressWarnings("unchecked")
    @Override public final V call() throws X, IOException {
        X ex = null;
        final InputStream in = source.input();
        try {
            return apply(in);
        } catch (Exception ex2) {
            throw ex = (X) ex2;
        } finally {
            try {
                in.close();
            } catch (IOException ex2) {
                if (null == ex) throw ex2;
            }
        }
    }

    protected abstract V apply(InputStream in) throws X;
}

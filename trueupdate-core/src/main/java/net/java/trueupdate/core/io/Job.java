/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.core.io;

import java.io.IOException;
import java.util.concurrent.Callable;

/**
 * A {@link Callable} for doing I/O.
 *
 * @author Christian Schlichtherle
 */
public interface Job<V, X extends Exception> extends Callable<V> {
    @Override V call() throws X, IOException;
}

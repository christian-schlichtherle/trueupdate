/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.core.io;

/**
 * A task operates on a resource and returns a result or throws an exception.
 *
 * @param <V> the type of the return value.
 * @param <R> the type of the resource parameter.
 * @param <X> the type of the exception.
 * @author Christian Schlichtherle
 */
public interface Task<V, R, X extends Exception> {

    V execute(R resource) throws X;
}

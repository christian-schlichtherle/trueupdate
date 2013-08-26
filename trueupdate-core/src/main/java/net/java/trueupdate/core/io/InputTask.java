/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.core.io;

import java.io.*;

/**
 * Executes a task on an {@link InputStream}.
 *
 * @see Sources#execute
 * @see Sources#bind
 * @author Christian Schlichtherle
 */
public interface InputTask<V, X extends Exception> {

    V execute(InputStream in) throws X;
}

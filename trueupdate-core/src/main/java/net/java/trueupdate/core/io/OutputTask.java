/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.core.io;

import java.io.*;

/**
 * Executes a task on an {@link OutputStream}.
 *
 * @see Sinks#execute
 * @see Sinks#bind
 * @author Christian Schlichtherle
 */
public interface OutputTask<V, X extends Exception> {

    V execute(OutputStream out) throws X;
}

/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.core.io;

import java.io.*;
import javax.annotation.WillNotClose;

/**
 * Executes a task on an {@link InputStream}.
 *
 * @see Sources#execute
 * @author Christian Schlichtherle
 */
public interface InputTask<V, X extends Exception>
extends Task<V, InputStream, X> {

    @Override V execute(@WillNotClose InputStream resource) throws X;
}

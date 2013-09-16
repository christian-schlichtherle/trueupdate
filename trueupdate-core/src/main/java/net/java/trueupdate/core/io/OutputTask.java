/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.core.io;

import java.io.*;
import javax.annotation.WillNotClose;

/**
 * Executes a task on an {@link OutputStream}.
 *
 * @see Sinks#execute
 * @author Christian Schlichtherle
 */
public interface OutputTask<V, X extends Exception>
extends Task<V, OutputStream, X> {

    @Override V execute(@WillNotClose OutputStream resource) throws X;
}

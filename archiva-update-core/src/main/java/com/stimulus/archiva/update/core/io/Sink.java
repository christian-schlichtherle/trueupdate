/*
 * Copyright (C) 2005-2013 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.core.io;

import edu.umd.cs.findbugs.annotations.CreatesObligation;
import java.io.*;

/**
 * An abstraction for writing binary data.
 *
 * @see    Source
 * @author Christian Schlichtherle
 */
public interface Sink {
    /**
     * Returns a new output stream for writing the binary data to this sink.
     */
    @CreatesObligation OutputStream output() throws IOException;
}

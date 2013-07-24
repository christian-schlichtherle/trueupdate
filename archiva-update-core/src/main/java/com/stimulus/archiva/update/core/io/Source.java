/*
 * Copyright (C) 2005-2013 Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.core.io;

import edu.umd.cs.findbugs.annotations.CreatesObligation;
import java.io.*;

/**
 * An abstraction for reading binary data.
 *
 * @see    Sink
 * @author Christian Schlichtherle
 */
public interface Source {
    /**
     * Returns a new input stream for reading the binary data from this source.
     */
    @CreatesObligation InputStream input() throws IOException;
}

/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.core.io;

import edu.umd.cs.findbugs.annotations.CreatesObligation;
import java.io.*;

/**
 * An abstraction for reading binary data.
 *
 * @see    Sink
 * @author Christian Schlichtherle (copied and edited from TrueLicense Core 2.3.1)
 */
public interface Source {
    /**
     * Returns a new input stream for reading the binary data from this source.
     */
    @CreatesObligation InputStream input() throws IOException;
}

/*
 * Copyright (C) 2005-2013 Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.server.jar.diff;

import com.stimulus.archiva.update.core.io.Source;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.*;

/**
 * Provides access to a JAR entry in a JAR file.
 *
 * @author Christian Schlichtherle
 */
interface EntryInFile extends Source {
    /**
     * Returns the JAR entry.
     * Clients should not modify the returned entry.
     */
    JarEntry entry();

    /** Returns an input stream for reading the JAR entry contents. */
    @Override public InputStream input() throws IOException;
}

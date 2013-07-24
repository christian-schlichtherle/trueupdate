/*
 * Copyright (C) 2005-2013 Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.server.jardiff;

import com.stimulus.archiva.update.core.io.Source;

import java.util.jar.JarEntry;

/**
 * A JAR entry in a JAR file.
 *
 * @author Christian Schlichtherle
 */
public interface EntryInFile extends Source {
    /**
     * Returns the JAR entry.
     * Clients should not modify the returned entry.
     */
    JarEntry entry();
}

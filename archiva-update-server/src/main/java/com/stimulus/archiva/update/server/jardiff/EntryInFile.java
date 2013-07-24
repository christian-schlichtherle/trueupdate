/*
 * Copyright (C) 2005-2013 Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.server.jardiff;

import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * A JAR entry in a JAR file.
 *
 * @author Christian Schlichtherle
 */
public interface EntryInFile {
    /** Returns the JAR entry. */
    JarEntry entry();

    /** Returns the JAR file. */
    JarFile file();
}

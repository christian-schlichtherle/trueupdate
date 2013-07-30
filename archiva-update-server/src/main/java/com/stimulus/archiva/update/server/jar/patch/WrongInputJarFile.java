/*
 * Copyright (C) 2005-2013 Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.server.jar.patch;

import java.io.IOException;

/**
 * Indicates that the input JAR file provided for patching doesn't match the
 * first JAR file when generating the JAR diff file.
 *
 * @author Christian Schlichtherle
 */
public final class WrongInputJarFile extends IOException {

    private static final long serialVersionUID = 0L;

    WrongInputJarFile(String message, Throwable cause) {
        super(message, cause);
    }
}

/*
 * Copyright (C) 2005-2013 Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.server.jar.patch;

import java.io.IOException;

/**
 * Indicates that the integrity of the JAR diff file has been violated.
 *
 * @author Christian Schlichtherle
 */
public final class InvalidJarPatchFileException extends IOException {

    private static final long serialVersionUID = 0L;

    InvalidJarPatchFileException(String message, Throwable cause) {
        super(message, cause);
    }
}

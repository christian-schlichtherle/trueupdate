/*
 * Copyright (C) 2005-2013 Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.server.jar.patch;

import java.io.IOException;

/**
 * Indicates that the input ZIP file provided for patching doesn't match the
 * first ZIP file when generating the ZIP patch file.
 *
 * @author Christian Schlichtherle
 */
public final class WrongInputZipFile extends IOException {

    private static final long serialVersionUID = 0L;

    WrongInputZipFile(String message, Throwable cause) {
        super(message, cause);
    }
}

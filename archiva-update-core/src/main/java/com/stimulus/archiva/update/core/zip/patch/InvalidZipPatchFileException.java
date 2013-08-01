/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.core.zip.patch;

import java.io.IOException;

/**
 * Indicates that the integrity of the ZIP patch file has been violated.
 *
 * @author Christian Schlichtherle
 */
public final class InvalidZipPatchFileException extends IOException {

    private static final long serialVersionUID = 0L;

    InvalidZipPatchFileException(String message, Throwable cause) {
        super(message, cause);
    }
}

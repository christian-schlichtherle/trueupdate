/*
 * Copyright (C) 2005-2013 Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.server.jar.patch;

import java.io.IOException;

/**
 * Indicates that a computed {@link java.security.MessageDigest} did not match
 * an expected message digest.
 *
 * @author Christian Schlichtherle
 */
public final class UnexpectedMessageDigestException extends IOException {

    private static final long serialVersionUID = 0L;

    public UnexpectedMessageDigestException(String message) {
        super(message);
    }
}

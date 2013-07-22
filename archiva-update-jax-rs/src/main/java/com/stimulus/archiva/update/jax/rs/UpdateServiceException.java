/*
 * Copyright (C) 2005-2013 Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.jax.rs;

import javax.annotation.CheckForNull;
import javax.annotation.concurrent.Immutable;

/**
 * Wraps an {@link Exception} in order to decorate it with additional meta data
 * for generating an HTTP response.
 *
 * @author Christian Schlichtherle
 */
@Immutable
public class UpdateServiceException extends Exception {

    private static final long serialVersionUID = 0L;

    private final int status;

    /**
     * Constructs an update service exception with the given HTTP
     * status code and its causing exception.
     *
     * @param status the HTTP status code.
     * @param cause the causing exception.
     * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html">http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html</a>
     */
    UpdateServiceException(
            final int status,
            final @CheckForNull Exception cause) {
        super(null == cause ? null : cause.getMessage(), cause);
        this.status = status;
    }

    /** Returns the HTTP status code. */
    public int getStatus() { return status; }
}

/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.jaxrs.util;

import java.io.IOException;
import javax.annotation.CheckForNull;

/**
 * Wraps a {@link Throwable} in order to decorate it with additional meta data
 * when producing or consuming a HTTP response.
 *
 * @author Christian Schlichtherle
 */
public class UpdateServiceException extends IOException {

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
    public UpdateServiceException(
            final int status,
            final @CheckForNull Throwable cause) {
        super(null == cause ? null : cause.toString(), cause);
        this.status = status;
    }

    /** Returns the HTTP status code. */
    public int getStatus() { return status; }
}

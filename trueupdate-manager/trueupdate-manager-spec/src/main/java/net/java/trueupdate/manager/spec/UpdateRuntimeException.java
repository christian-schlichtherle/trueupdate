/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.spec;

import javax.annotation.CheckForNull;

/**
 * @author Christian Schlichtherle
 */
public class UpdateRuntimeException extends RuntimeException {

    private static final long serialVersionUID = 0L;

    public UpdateRuntimeException(@CheckForNull Throwable cause) {
        super(null == cause ? null : cause.toString(), cause);
    }
}

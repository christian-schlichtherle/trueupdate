/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.spec;

import javax.annotation.CheckForNull;

/**
 * @author Christian Schlichtherle
 */
public class UpdateException extends Exception {

    private static final long serialVersionUID = 0L;

    public UpdateException(@CheckForNull Throwable cause) {
        super(null == cause ? null : cause.toString(), cause);
    }
}

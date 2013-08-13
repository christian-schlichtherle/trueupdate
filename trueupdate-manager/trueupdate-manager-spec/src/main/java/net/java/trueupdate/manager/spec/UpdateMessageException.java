/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.spec;

import javax.annotation.CheckForNull;

/**
 * Indicates an error while processing an {@link UpdateMessage}.
 *
 * @author Christian Schlichtherle
 */
public class UpdateMessageException extends Exception {

    private static final long serialVersionUID = 0L;

    public UpdateMessageException(@CheckForNull Throwable cause) {
        super(null == cause ? null : cause.toString(), cause);
    }
}

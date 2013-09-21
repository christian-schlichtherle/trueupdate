/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.core;

import java.io.IOException;
import javax.annotation.CheckForNull;

/**
 * Thrown by an update manager to indicate an error condition while processing
 * messages.
 *
 * @author Christian Schlichtherle
 */
public class UpdateManagerException extends IOException {

    private static final long serialVersionUID = 0L;

    public UpdateManagerException(@CheckForNull Throwable cause) {
        super(null == cause ? null : cause.toString(), cause);
    }
}

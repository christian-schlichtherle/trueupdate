/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.agent.spec;

import java.io.IOException;
import javax.annotation.CheckForNull;

/**
 * Thrown by an update agent to indicate an error condition while processing
 * messages.
 *
 * @author Christian Schlichtherle
 */
public class UpdateAgentException extends IOException {

    private static final long serialVersionUID = 0L;

    public UpdateAgentException(@CheckForNull Throwable cause) {
        super(null == cause ? null : cause.toString(), cause);
    }
}

/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.agent.api;

import javax.annotation.CheckForNull;
import net.java.trueupdate.manager.api.UpdateMessage;

/**
 * Indicates an error while sending an {@link UpdateMessage}.
 *
 * @author Christian Schlichtherle
 */
public class UpdateAgentException extends Exception {

    private static final long serialVersionUID = 0L;

    public UpdateAgentException(@CheckForNull Throwable cause) {
        super(null == cause ? null : cause.toString(), cause);
    }
}

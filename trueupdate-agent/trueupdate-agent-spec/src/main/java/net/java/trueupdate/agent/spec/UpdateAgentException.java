/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.agent.spec;

import javax.annotation.CheckForNull;
import net.java.trueupdate.manager.spec.UpdateMessage;

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

/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.agent.spec;

import net.java.trueupdate.message.UpdateMessage;

/**
 * An update agent event.
 * <p>
 * Implementations must be thread-safe.
 *
 * @author Christian Schlichtherle
 */
public interface UpdateAgentEvent {

    /** Returns the update agent. */
    UpdateAgent updateAgent();

    /** Returns the update message. */
    UpdateMessage updateMessage();
}

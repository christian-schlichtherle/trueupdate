/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.agent.spec;

import net.java.trueupdate.manager.spec.UpdateMessage;

/**
 * An update agent event.
 * <p>
 * Applications have no need to implement this class and should not do so
 * because it may be subject to future expansion.
 * <p>
 * Implementations must be thread-safe.
 * <p>
 * Applications have no need to implement this class and should not do so
 * because it may be subject to future expansion.
 *
 * @author Christian Schlichtherle
 */
public interface UpdateAgentEvent {

    /** Returns the update agent. */
    UpdateAgent updateAgent();

    /** Returns the update message. */
    UpdateMessage updateMessage();
}

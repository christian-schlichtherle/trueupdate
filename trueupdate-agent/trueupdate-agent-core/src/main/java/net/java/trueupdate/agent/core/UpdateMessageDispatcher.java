/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.agent.core;

import net.java.trueupdate.agent.spec.ApplicationParameters;
import net.java.trueupdate.manager.spec.UpdateMessageListener;

/**
 * An update agent dispatcher is an update message listener which dispatches
 * the update messages to application listeners.
 *
 * @author Christian Schlichtherle
 */
public interface UpdateMessageDispatcher extends UpdateMessageListener {

    /**
     * Subscribes to update messages for the application descriptor by using
     * the application listener in the given application parameters.
     *
     * @param parameters the application parameters.
     */
    void subscribe(ApplicationParameters parameters);

    /**
     * Unsubscribes from update messages for the application descriptor in the
     * given application parameters.
     *
     * @param parameters the application parameters.
     */
    void unsubscribe(ApplicationParameters parameters);
}

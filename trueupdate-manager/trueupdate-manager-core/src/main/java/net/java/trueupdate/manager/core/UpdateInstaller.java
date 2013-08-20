/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.core;

import net.java.trueupdate.manager.api.UpdateMessage;

/**
 * Cooperates with an update resolver to install artifact updates.
 * <p>
 * Applications have no need to implement this class and should not do so
 * because it may be subject to future expansion.
 *
 * @author Christian Schlichtherle
 */
public interface UpdateInstaller {

    /**
     * Cooperates with the given update resolver to install the artifact update
     * as described in the given update message.
     *
     * @param resolver the update resolver.
     * @param message the update message.
     * @throws Exception at the discretion of the implementation.
     */
    void install(UpdateResolver resolver, UpdateMessage message)
    throws Exception;
}

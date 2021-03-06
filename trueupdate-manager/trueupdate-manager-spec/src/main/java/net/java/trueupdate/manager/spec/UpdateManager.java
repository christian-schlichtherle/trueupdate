/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.spec;

/**
 * An update manager cooperates with update agents to automatically install
 * updates to an application.
 * All communication between the update agent and the update manager is
 * asynchronous.
 * <p>
 * Implementations must be thread-safe.
 *
 * @author Christian Schlichtherle
 */
public interface UpdateManager {

    /** Checks for updates and notifies the subscribed agents. */
    void checkForUpdates() throws Exception;

    /**
     * Stops this update manager.
     * This method is idempotent.
     * However, it's the caller's responsibility to make sure that this update
     * manager isn't used anymore after the call to this method, even if it
     * fails.
     */
    void close() throws Exception;
}

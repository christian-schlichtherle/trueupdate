/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.agent.spec;

/**
 * An update agent cooperates with an update manager to automatically install
 * updates to an application.
 * All communication between the update agent and the update manager is
 * asynchronous.
 * <p>
 * Implementations should be immutable and hence, thread-safe.
 * <p>
 * Applications have no need to implement this class and should not do so
 * because it may be subject to future expansion.
 *
 * @author Christian Schlichtherle
 */
public interface UpdateAgent {

    /**
     * Sends a request to subscribe to the list of recipients for update
     * announcements for the application.
     *
     * @throws UpdateAgentException if sending the request is not possible
     *         for some reason.
     */
    void subscribe() throws UpdateAgentException;

    /**
     * Sends a request to shutdown the application, install the given version
     * and restart it.
     *
     * @param version the version to install.
     *        Specifying the current version shows no effect.
     *        Specifying a higher version upgrades the application.
     *        Specifying a prior version downgrades the application.
     * @throws UpdateAgentException if sending the request is not possible
     *         for some reason.
     */
    void install(String version) throws UpdateAgentException;

    /**
     * Sends a request to unsubscribe from the list of recipients for update
     * announcements for the application.
     *
     * @throws UpdateAgentException if sending the request is not possible
     *         for some reason.
     */
    void unsubscribe() throws UpdateAgentException;
}

/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.agent.spec;

/**
 * An update agent cooperates with an update manager to automatically install
 * updates to an application.
 * All communication between the update agent and the update manager is
 * asynchronous.
 * <p>
 * Implementations must be thread-safe.
 *
 * @author Christian Schlichtherle
 */
public interface UpdateAgent {

    /**
     * Sends a request to subscribe to the list of recipients for update
     * notices for the application.
     */
    void subscribe() throws Exception;

    /**
     * Sends a request to install the given artifact version.
     *
     * @param version the artifact version to install.
     *        Specifying the current version shows no effect.
     *        Specifying a higher version upgrades the application.
     *        Specifying a prior version downgrades the application.
     */
    void install(String version) throws Exception;

    /**
     * Sends a positive response to a redeployment request in order to
     * complete the installation of an artifact update.
     * On success, this will subsequently shutdown this application, install
     * the update and restart the updated application.
     */
    void proceed() throws Exception;

    /**
     * Sends a negative response to a redeployment request in order to
     * cancel the installation of an artifact update.
     */
    void cancel() throws Exception;

    /**
     * Sends a request to unsubscribe from the list of recipients for update
     * notices for the application.
     */
    void unsubscribe() throws Exception;
}

/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.agent.spec;

/**
 * Listens to events from the update agent.
 *
 * @author Christian Schlichtherle
 */
public class UpdateAgentListener {

    public void onSubscriptionSuccessResponse(UpdateAgentEvent event)
    throws Exception { }

    public void onSubscriptionFailureResponse(UpdateAgentEvent event)
    throws Exception { }

    /**
     * Responds to the update notice event.
     * <p>
     * The implementation in the class {@link UpdateAgentListener} sends a
     * request to install the available artifact update version.
     */
    public void onUpdateNotice(UpdateAgentEvent event)
    throws Exception {
        event.updateAgent().install(event.updateMessage().updateVersion());
    }

    public void onInstallationSuccessResponse(UpdateAgentEvent event)
    throws Exception { }

    public void onInstallationFailureResponse(UpdateAgentEvent event)
    throws Exception { }

    public void onUnsubscriptionSuccessResponse(UpdateAgentEvent event)
    throws Exception { }

    public void onUnsubscriptionFailureResponse(UpdateAgentEvent event)
    throws Exception { }
}

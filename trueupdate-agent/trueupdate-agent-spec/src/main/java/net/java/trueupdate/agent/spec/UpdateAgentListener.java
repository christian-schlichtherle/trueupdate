/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
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

    public void onUpdateNotice(UpdateAgentEvent event)
    throws Exception { }

    public void onInstallationSuccessResponse(UpdateAgentEvent event)
    throws Exception { }

    public void onInstallationFailureResponse(UpdateAgentEvent event)
    throws Exception { }

    public void onUnsubscriptionSuccessResponse(UpdateAgentEvent event)
    throws Exception { }

    public void onUnsubscriptionFailureResponse(UpdateAgentEvent event)
    throws Exception { }
}

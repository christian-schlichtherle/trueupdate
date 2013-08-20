/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.agent.api;

import net.java.trueupdate.manager.api.UpdateMessage;

/**
 * Processes update messages from an update manager on behalf of the
 * application.
 *
 * @author Christian Schlichtherle
 */
public class ApplicationListener {

    public void onSubscriptionSuccessResponse(UpdateMessage message)
    throws Exception { }

    public void onSubscriptionFailureResponse(UpdateMessage message)
    throws Exception { }

    public void onUpdateNotice(UpdateMessage message)
    throws Exception { }

    public void onInstallationSuccessResponse(UpdateMessage message)
    throws Exception { }

    public void onInstallationFailureResponse(UpdateMessage message)
    throws Exception { }

    public void onUnsubscriptionSuccessResponse(UpdateMessage message)
    throws Exception { }

    public void onUnsubscriptionFailureResponse(UpdateMessage message)
    throws Exception { }
}

/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.agent.spec;

import net.java.trueupdate.manager.spec.UpdateMessage;
import net.java.trueupdate.manager.spec.UpdateMessageException;

/**
 * Processes update messages from an update manager on behalf of the
 * application.
 *
 * @author Christian Schlichtherle
 */
public class ApplicationListener {

    public void onSubscriptionSuccessResponse(UpdateMessage message)
    throws UpdateMessageException { }

    public void onSubscriptionFailureResponse(UpdateMessage message)
    throws UpdateMessageException { }

    public void onUpdateAnnouncement(UpdateMessage message)
    throws UpdateMessageException { }

    public void onInstallationSuccessResponse(UpdateMessage message)
    throws UpdateMessageException { }

    public void onInstallationFailureResponse(UpdateMessage message)
    throws UpdateMessageException { }

    public void onUnsubscriptionSuccessResponse(UpdateMessage message)
    throws UpdateMessageException { }

    public void onUnsubscriptionFailureResponse(UpdateMessage message)
    throws UpdateMessageException { }
}

/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.agent.spec;

import net.java.trueupdate.message.UpdateMessage;
import net.java.trueupdate.message.UpdateMessageException;

/**
 * Processes update messages from an update manager.
 *
 * @author Christian Schlichtherle
 */
public abstract class UpdateManagerListener {

    protected void onSubscriptionSuccessResponse(UpdateMessage message)
    throws UpdateMessageException { }

    protected void onSubscriptionFailureResponse(UpdateMessage message)
    throws UpdateMessageException { }

    protected void onUpdateAnnouncement(UpdateMessage message)
    throws UpdateMessageException { }

    protected void onInstallationSuccessResponse(UpdateMessage message)
    throws UpdateMessageException { }

    protected void onInstallationFailureResponse(UpdateMessage message)
    throws UpdateMessageException { }

    protected void onUnsubscriptionSuccessResponse(UpdateMessage message)
    throws UpdateMessageException { }

    protected void onUnsubscriptionFailureResponse(UpdateMessage message)
    throws UpdateMessageException { }
}

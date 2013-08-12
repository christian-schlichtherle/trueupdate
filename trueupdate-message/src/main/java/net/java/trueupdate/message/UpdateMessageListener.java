/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.message;

/**
 * Processes update messages.
 *
 * @author Christian Schlichtherle
 */
public abstract class UpdateMessageListener {

    /**
     * Processes the given update message by dispatching the call to the
     * corresponding {@code visit<Type>(UpdateMessage)} method, where
     * {@code <Type>} is the {@link UpdateMessage.Type} obtained from calling
     * the {@link UpdateMessage#type()} method.
     *
     * @param message the update message to process.
     */
    public void onUpdateMessage(UpdateMessage message)
    throws UpdateMessageException {
        message.type().dispatchMessageTo(message, this);
    }

    protected void onSubscriptionRequest(UpdateMessage message)
    throws UpdateMessageException { }

    protected void onSubscriptionSuccessResponse(UpdateMessage message)
    throws UpdateMessageException { }

    protected void onSubscriptionFailureResponse(UpdateMessage message)
    throws UpdateMessageException { }

    protected void onUpdateAnnouncement(UpdateMessage message)
    throws UpdateMessageException { }

    protected void onInstallationRequest(UpdateMessage message)
    throws UpdateMessageException { }

    protected void onInstallationSuccessResponse(UpdateMessage message)
    throws UpdateMessageException { }

    protected void onInstallationFailureResponse(UpdateMessage message)
    throws UpdateMessageException { }

    protected void onUnsubscriptionRequest(UpdateMessage message)
    throws UpdateMessageException { }

    protected void onUnsubscriptionSuccessResponse(UpdateMessage message)
    throws UpdateMessageException { }

    protected void onUnsubscriptionFailureResponse(UpdateMessage message)
    throws UpdateMessageException { }
}

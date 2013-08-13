/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.spec;

/**
 * An update message listener which filters and dispatches the update messages
 * to its protected stub methods.
 *
 * @author Christian Schlichtherle
 */
public class UpdateMessageDispatcher implements UpdateMessageListener {

    /** Returns the filter to use before dispatching an update message. */
    protected UpdateMessageFilter filter() {
        return UpdateMessageFilter.ACCEPT_ALL;
    }

    /**
     * Filters the given update message and dispatches the call to the
     * corresponding {@code visit<Type>(UpdateMessage)} method, where
     * {@code <Type>} is the {@link UpdateMessage.Type} obtained from calling
     * the {@link UpdateMessage#type()} method.
     *
     * @param message the update message to process.
     */
    @Override
    public void onUpdateMessage(UpdateMessage message)
    throws UpdateMessageException {
        if (filter().accept(message))
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

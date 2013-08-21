/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.spec;

import javax.annotation.concurrent.Immutable;

/**
 * An update message listener which filters and dispatches the update messages
 * to its protected stub methods.
 *
 * @author Christian Schlichtherle
 */
@Immutable
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
     */
    @Override
    public void onUpdateMessage(UpdateMessage message) throws Exception {
        if (filter().accept(message))
            message.type().dispatchMessageTo(message, this);
    }

    protected void onSubscriptionNotice(UpdateMessage message)
    throws Exception { }

    protected void onSubscriptionRequest(UpdateMessage message)
    throws Exception { }

    protected void onSubscriptionSuccessResponse(UpdateMessage message)
    throws Exception { }

    protected void onSubscriptionFailureResponse(UpdateMessage message)
    throws Exception { }

    protected void onUpdateNotice(UpdateMessage message)
    throws Exception { }

    protected void onInstallationRequest(UpdateMessage message)
    throws Exception { }

    protected void onInstallationSuccessResponse(UpdateMessage message)
    throws Exception { }

    protected void onInstallationFailureResponse(UpdateMessage message)
    throws Exception { }

    protected void onUnsubscriptionNotice(UpdateMessage message)
    throws Exception { }

    protected void onUnsubscriptionRequest(UpdateMessage message)
    throws Exception { }

    protected void onUnsubscriptionSuccessResponse(UpdateMessage message)
    throws Exception { }

    protected void onUnsubscriptionFailureResponse(UpdateMessage message)
    throws Exception { }
}

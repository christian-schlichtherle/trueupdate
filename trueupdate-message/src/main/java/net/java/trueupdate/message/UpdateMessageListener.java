/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.message;

import javax.annotation.concurrent.Immutable;

/**
 * An update message listener which filters and dispatches the update messages
 * to its protected stub methods.
 *
 * @author Christian Schlichtherle
 */
@Immutable
public abstract class UpdateMessageListener {

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
    public void onUpdateMessage(UpdateMessage message) throws Exception {
        if (filter().accept(message))
            message.type().dispatchMessageTo(message, this);
    }

    protected void onSubscriptionNotice(UpdateMessage message)
    throws Exception { }

    protected void onSubscriptionRequest(UpdateMessage message)
    throws Exception { }

    protected void onSubscriptionResponse(UpdateMessage message)
    throws Exception { }

    protected void onUpdateNotice(UpdateMessage message)
    throws Exception { }

    protected void onInstallationRequest(UpdateMessage message)
    throws Exception { }

    protected void onProgressNotice(UpdateMessage message)
    throws Exception { }

    protected void onRedeploymentRequest(UpdateMessage message)
    throws Exception { }

    protected void onProceedRedeploymentResponse(UpdateMessage message)
    throws Exception { }

    protected void onCancelRedeploymentResponse(UpdateMessage message)
    throws Exception { }

    protected void onInstallationSuccessResponse(UpdateMessage message)
    throws Exception { }

    protected void onInstallationFailureResponse(UpdateMessage message)
    throws Exception { }

    protected void onUnsubscriptionNotice(UpdateMessage message)
    throws Exception { }
}

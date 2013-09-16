/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.agent.mini;

import net.java.trueupdate.agent.spec.ApplicationListener;
import net.java.trueupdate.agent.spec.ApplicationParameters;
import net.java.trueupdate.manager.spec.ApplicationDescriptor;
import net.java.trueupdate.manager.spec.UpdateMessage;
import net.java.trueupdate.manager.spec.UpdateMessageFilter;
import net.java.trueupdate.manager.spec.UpdateMessageListener;

/**
 * Listens to update messages and forwards them to the configured application
 * listener if and only if the configured application descriptor equals the
 * application descriptor in the update message.
 *
 * @author Christian Schlichtherle
 */
final class MiniUpdateMessageListener extends UpdateMessageListener {

    private final ApplicationListener listener;
    private final UpdateMessageFilter filter;

    MiniUpdateMessageListener(final ApplicationParameters parameters) {
        this.listener = parameters.applicationListener();
        this.filter = new UpdateMessageFilter() {
            final ApplicationDescriptor applicationDescriptor =
                    parameters.applicationDescriptor();

            @Override public boolean accept(UpdateMessage message) {
                return applicationDescriptor.equals(
                        message.applicationDescriptor());
            }
        };
    }

    @Override protected UpdateMessageFilter filter() { return filter; }

    @Override
    protected void onSubscriptionSuccessResponse(UpdateMessage message)
    throws Exception {
        listener.onSubscriptionSuccessResponse(message);
    }

    @Override
    protected void onSubscriptionFailureResponse(UpdateMessage message)
    throws Exception {
        listener.onSubscriptionFailureResponse(message);
    }

    @Override
    protected void onUpdateNotice(UpdateMessage message)
    throws Exception {
        listener.onUpdateNotice(message);
    }

    @Override
    protected void onInstallationSuccessResponse(UpdateMessage message)
    throws Exception {
        listener.onInstallationSuccessResponse(message);
    }

    @Override
    protected void onInstallationFailureResponse(UpdateMessage message)
    throws Exception {
        listener.onInstallationFailureResponse(message);
    }

    @Override
    protected void onUnsubscriptionSuccessResponse(UpdateMessage message)
    throws Exception {
        listener.onUnsubscriptionSuccessResponse(message);
    }

    @Override
    protected void onUnsubscriptionFailureResponse(UpdateMessage message)
    throws Exception {
        listener.onUnsubscriptionFailureResponse(message);
    }
}
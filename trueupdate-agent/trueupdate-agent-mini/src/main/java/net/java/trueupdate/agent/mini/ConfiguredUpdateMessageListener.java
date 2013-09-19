/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.agent.mini;

import javax.annotation.concurrent.Immutable;
import net.java.trueupdate.agent.spec.*;
import net.java.trueupdate.manager.spec.*;
import net.java.trueupdate.util.Objects;

/**
 * Listens to update messages and forwards them to the configured application
 * listener if and only if the configured application descriptor equals the
 * application descriptor in the update message.
 *
 * @author Christian Schlichtherle
 */
@Immutable
final class ConfiguredUpdateMessageListener extends UpdateMessageListener {

    private final UpdateAgent agent;
    private final UpdateAgentListener listener;
    private final UpdateMessageFilter filter;

    ConfiguredUpdateMessageListener(
            final UpdateAgent agent,
            final ApplicationParameters parameters) {
        this.agent = Objects.requireNonNull(agent);
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
        listener.onSubscriptionSuccessResponse(event(message));
    }

    @Override
    protected void onSubscriptionFailureResponse(UpdateMessage message)
    throws Exception {
        listener.onSubscriptionFailureResponse(event(message));
    }

    @Override
    protected void onUpdateNotice(UpdateMessage message)
    throws Exception {
        listener.onUpdateNotice(event(message));
    }

    @Override
    protected void onInstallationSuccessResponse(UpdateMessage message)
    throws Exception {
        listener.onInstallationSuccessResponse(event(message));
    }

    @Override
    protected void onInstallationFailureResponse(UpdateMessage message)
    throws Exception {
        listener.onInstallationFailureResponse(event(message));
    }

    @Override
    protected void onUnsubscriptionSuccessResponse(UpdateMessage message)
    throws Exception {
        listener.onUnsubscriptionSuccessResponse(event(message));
    }

    @Override
    protected void onUnsubscriptionFailureResponse(UpdateMessage message)
    throws Exception {
        listener.onUnsubscriptionFailureResponse(event(message));
    }

    private UpdateAgentEvent event(final UpdateMessage message) {
        return new UpdateAgentEvent() {
            @Override public UpdateAgent updateAgent() { return agent; }
            @Override public UpdateMessage updateMessage() { return message; }
        };
    }
}

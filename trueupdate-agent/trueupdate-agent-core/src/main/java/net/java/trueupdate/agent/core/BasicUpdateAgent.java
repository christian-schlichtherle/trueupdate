/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.agent.core;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import net.java.trueupdate.agent.spec.*;
import net.java.trueupdate.message.*;
import static net.java.trueupdate.message.UpdateMessage.Type.*;

/**
 * A basic update agent.
 *
 * @author Christian Schlichtherle
 */
@Immutable
public abstract class BasicUpdateAgent
extends UpdateMessageListener implements UpdateAgent {

    private volatile UpdateMessageFilter filter;

    protected abstract ApplicationParameters applicationParameters();

    protected abstract String from();

    protected abstract String to();

    @Override public void subscribe() throws Exception {
        send(SUBSCRIPTION_REQUEST, null);
    }

    @Override public void install(String version) throws Exception {
        send(INSTALLATION_REQUEST, version);
    }

    @Override public void proceed() throws Exception {
        send(PROCEED_REDEPLOYMENT_RESPONSE, null);
    }

    @Override public void cancel() throws Exception {
        send(CANCEL_REDEPLOYMENT_RESPONSE, null);
    }

    @Override public void unsubscribe() throws Exception {
        send(UNSUBSCRIPTION_NOTICE, null);
    }

    private void send(final UpdateMessage.Type type,
                      final @Nullable String updateVersion)
    throws Exception {
        final ApplicationParameters ap = applicationParameters();
        final UpdateMessage message = UpdateMessage
                    .builder()
                    .from(from())
                    .to(to())
                    .type(type)
                    .artifactDescriptor(ap.artifactDescriptor())
                    .currentLocation(ap.currentLocation())
                    .updateLocation(ap.updateLocation())
                    .updateVersion(updateVersion)
                    .build();
        send(message);
    }

    protected abstract void send(UpdateMessage message) throws Exception;

    @Override protected UpdateMessageFilter filter() {
        final UpdateMessageFilter f = filter;
        return null != f ? f : (filter = newFilter());
    }

    private UpdateMessageFilter newFilter() {
        return new UpdateMessageFilter() {
            final ApplicationDescriptor applicationDescriptor =
                    applicationParameters().applicationDescriptor();

            @Override public boolean accept(UpdateMessage message) {
                return applicationDescriptor.equals(
                        message.applicationDescriptor());
            }
        };
    }

    @Override
    protected void onSubscriptionResponse(UpdateMessage message)
    throws Exception {
        listener().onSubscriptionResponse(event(message));
    }

    @Override
    protected void onUpdateNotice(UpdateMessage message)
    throws Exception {
        listener().onUpdateNotice(event(message));
    }

    @Override
    protected void onProgressNotice(UpdateMessage message)
    throws Exception {
        listener().onProgressNotice(event(message));
    }

    @Override
    protected void onRedeploymentRequest(UpdateMessage message)
    throws Exception {
        listener().onRedeploymentRequest(event(message));
    }

    @Override
    protected void onInstallationSuccessResponse(UpdateMessage message)
    throws Exception {
        listener().onInstallationSuccessResponse(event(message));
    }

    @Override
    protected void onInstallationFailureResponse(UpdateMessage message)
    throws Exception {
        listener().onInstallationFailureResponse(event(message));
    }

    private UpdateAgentListener listener() {
        return applicationParameters().updateAgentListener();
    }

    private UpdateAgentEvent event(final UpdateMessage message) {
        return new UpdateAgentEvent() {
            @Override public UpdateAgent updateAgent() {
                return BasicUpdateAgent.this;
            }

            @Override public UpdateMessage updateMessage() { return message; }
        };
    }
}

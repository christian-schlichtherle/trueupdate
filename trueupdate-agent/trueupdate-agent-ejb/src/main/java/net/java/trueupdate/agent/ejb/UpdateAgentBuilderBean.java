/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.agent.ejb;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.CheckForNull;
import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import net.java.trueupdate.agent.spec.ApplicationDescriptor;
import net.java.trueupdate.agent.spec.UpdateAgent;
import net.java.trueupdate.agent.spec.UpdateAgent.Builder;
import net.java.trueupdate.agent.spec.UpdateAgent.Parameters;
import net.java.trueupdate.agent.spec.UpdateAgent.UpdateListener;
import net.java.trueupdate.message.UpdateMessage;
import net.java.trueupdate.message.UpdateMessageDispatcher;
import net.java.trueupdate.message.UpdateMessageException;

/**
 * @author Christian Schlichtherle
 */
@Singleton
@SuppressWarnings("PackageVisibleField")
public class UpdateAgentBuilderBean
extends UpdateMessageDispatcher implements UpdateAgentBuilder {

    private static final Logger
            logger = Logger.getLogger(UpdateAgentBuilderBean.class.getName());

    static final String DESTINATION_NAME = "jms/trueupdate";

    private static final UpdateListener NULL = new UpdateListener();

    @Resource ConnectionFactory connectionFactory;
    @Resource(lookup = DESTINATION_NAME) Destination destination;
    @CheckForNull Parameters parameters;

    @Override public Parameters.Builder<Builder> parameters() {
        return new Parameters.Builder<Builder>() {
            @Override public Builder inject() { return parameters(build()); }
        };
    }

    @Override public Builder parameters(final Parameters parameters) {
        if (anotherApplicationDescriptor(parameters))
            throw new IllegalArgumentException("Cannot create an update agent for another application descriptor.");
        this.parameters = parameters;
        return this;
    }

    private boolean anotherApplicationDescriptor(final Parameters parameters) {
        final ApplicationDescriptor ad = applicationDescriptor();
        return null != ad && !ad.equals(parameters.applicationDescriptor());
    }

    private @CheckForNull ApplicationDescriptor applicationDescriptor() {
        return null == parameters ? null : parameters.applicationDescriptor();
    }

    @Override public UpdateAgent build() { return new BasicUpdateAgent(this); }

    @Override
    protected void onSubscriptionSuccessResponse(final UpdateMessage message)
    throws UpdateMessageException {
        log(message);
        listener(message).onSubscriptionSuccessResponse(message);
    }

    @Override
    protected void onSubscriptionFailureResponse(final UpdateMessage message)
    throws UpdateMessageException {
        log(message);
        listener(message).onSubscriptionFailureResponse(message);
    }

    @Override
    protected void onUpdateAnnouncement(final UpdateMessage message)
    throws UpdateMessageException {
        log(message);
        listener(message).onUpdateAnnouncement(message);
    }

    @Override
    protected void onInstallationSuccessResponse(final UpdateMessage message)
    throws UpdateMessageException {
        log(message);
        listener(message).onInstallationSuccessResponse(message);
    }

    @Override
    protected void onInstallationFailureResponse(final UpdateMessage message)
    throws UpdateMessageException {
        log(message);
        listener(message).onInstallationFailureResponse(message);
    }

    @Override
    protected void onUnsubscriptionSuccessResponse(final UpdateMessage message)
    throws UpdateMessageException {
        log(message);
        listener(message).onUnsubscriptionSuccessResponse(message);
    }

    @Override
    protected void onUnsubscriptionFailureResponse(final UpdateMessage message)
    throws UpdateMessageException {
        log(message);
        listener(message).onUnsubscriptionFailureResponse(message);
    }

    private UpdateMessage log(final UpdateMessage message) {
        logger.log(Level.INFO, "Received update message:\n{0}", message);
        return message;
    }

    private UpdateListener listener(UpdateMessage message) {
        return applicationDescriptor(message).equals(applicationDescriptor())
                    ? parameters.updateListener()
                    : NULL;

    }

    private ApplicationDescriptor applicationDescriptor(UpdateMessage message) {
        return ApplicationDescriptor
                .builder()
                .artifactDescriptor(message.artifactDescriptor())
                .currentLocation(message.currentLocation())
                .build();
    }
}

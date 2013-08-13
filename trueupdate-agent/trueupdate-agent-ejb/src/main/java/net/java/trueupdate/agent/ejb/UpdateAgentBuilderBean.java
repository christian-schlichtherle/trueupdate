/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.agent.ejb;

import java.util.logging.*;
import javax.annotation.*;
import javax.ejb.*;
import javax.jms.*;
import net.java.trueupdate.agent.spec.*;
import net.java.trueupdate.agent.spec.UpdateAgent.Builder;
import net.java.trueupdate.manager.spec.*;

/**
 * @author Christian Schlichtherle
 */
@Singleton
@SuppressWarnings("PackageVisibleField")
@Local({ UpdateMessageListener.class, UpdateAgent.Builder.class })
public class UpdateAgentBuilderBean
extends UpdateMessageDispatcher
implements UpdateMessageListener, UpdateAgent.Builder {

    private static final Logger
            logger = Logger.getLogger(UpdateAgentBuilderBean.class.getName());

    static final String DESTINATION_NAME = "jms/trueupdate";

    private static final ApplicationListener NULL = new ApplicationListener();

    @Resource ConnectionFactory connectionFactory;
    @Resource(lookup = DESTINATION_NAME) Destination destination;
    @CheckForNull ApplicationParameters applicationParameters;

    private final UpdateMessageFilter filter = new UpdateMessageFilter() {
        @Override public boolean accept(UpdateMessage message) {
            return message.applicationDescriptor().equals(applicationDescriptor());
        }
    };

    @Override protected UpdateMessageFilter filter() { return filter; }

    @Override public ApplicationParameters.Builder<Builder> applicationParameters() {
        return new ApplicationParameters.Builder<Builder>() {
            @Override public Builder inject() { return applicationParameters(build()); }
        };
    }

    @Override public Builder applicationParameters(
            final ApplicationParameters parameters) {
        if (anotherApplicationDescriptor(parameters))
            throw new IllegalArgumentException(
                    "Cannot create an update agent for another application descriptor.");
        this.applicationParameters = parameters;
        return this;
    }

    @Override public UpdateAgent build() { return new BasicUpdateAgent(this); }

    @Override
    protected void onSubscriptionSuccessResponse(final UpdateMessage message)
    throws UpdateMessageException {
        log(message);
        listener().onSubscriptionSuccessResponse(message);
    }

    @Override
    protected void onSubscriptionFailureResponse(final UpdateMessage message)
    throws UpdateMessageException {
        log(message);
        listener().onSubscriptionFailureResponse(message);
    }

    @Override
    protected void onUpdateAnnouncement(final UpdateMessage message)
    throws UpdateMessageException {
        log(message);
        listener().onUpdateAnnouncement(message);
    }

    @Override
    protected void onInstallationSuccessResponse(final UpdateMessage message)
    throws UpdateMessageException {
        log(message);
        listener().onInstallationSuccessResponse(message);
    }

    @Override
    protected void onInstallationFailureResponse(final UpdateMessage message)
    throws UpdateMessageException {
        log(message);
        listener().onInstallationFailureResponse(message);
    }

    @Override
    protected void onUnsubscriptionSuccessResponse(final UpdateMessage message)
    throws UpdateMessageException {
        log(message);
        listener().onUnsubscriptionSuccessResponse(message);
    }

    @Override
    protected void onUnsubscriptionFailureResponse(final UpdateMessage message)
    throws UpdateMessageException {
        log(message);
        listener().onUnsubscriptionFailureResponse(message);
    }

    private boolean anotherApplicationDescriptor(
            final ApplicationParameters parameters) {
        final ApplicationDescriptor ad = applicationDescriptor();
        return null != ad && !ad.equals(parameters.applicationDescriptor());
    }

    private ApplicationListener listener() {
        assert null != applicationParameters : "Error in update message filter.";
        return applicationParameters.applicationListener();
    }

    private @CheckForNull ApplicationDescriptor applicationDescriptor() {
        final ApplicationParameters ap = applicationParameters;
        return null == ap ? null : ap.applicationDescriptor();
    }

    private static UpdateMessage log(final UpdateMessage message) {
        logger.log(Level.FINE, "Received update message:\n{0}", message);
        return message;
    }
}

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
import net.java.trueupdate.agent.spec.UpdateAgent.UpdateListener;
import net.java.trueupdate.agent.spec.UpdateAgent.Parameters;
import net.java.trueupdate.message.UpdateMessage;
import net.java.trueupdate.message.UpdateMessageException;
import net.java.trueupdate.message.UpdateMessageListener;

/**
 * @author Christian Schlichtherle
 */
@Singleton
@SuppressWarnings("PackageVisibleField")
public class UpdateMessageListenerBean
extends UpdateMessageListener implements Builder {

    private static final Logger
            logger = Logger.getLogger(UpdateMessageListenerBean.class.getName());

    static final String JNDI_NAME = "jms/trueupdate";

    private static final UpdateListener NULL = new UpdateListener() { };

    @Resource ConnectionFactory connectionFactory;
    @Resource(lookup = JNDI_NAME) Destination destination;
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
        logger.log(Level.INFO, "Received subscription success response:\n{0}", message.toString());
        listener().onSubscriptionSuccessResponse(message);
    }

    @Override
    protected void onSubscriptionFailureResponse(final UpdateMessage message)
    throws UpdateMessageException {
        logger.log(Level.INFO, "Received subscription failure response:\n{0}", message.toString());
        listener().onSubscriptionFailureResponse(message);
    }

    @Override
    protected void onUpdateAnnouncement(final UpdateMessage message)
    throws UpdateMessageException {
        logger.log(Level.INFO, "Received update announcement:\n{0}", message.toString());
        listener().onUpdateAnnouncement(message);
    }

    @Override
    protected void onInstallationSuccessResponse(final UpdateMessage message)
    throws UpdateMessageException {
        logger.log(Level.INFO, "Received installation success response:\n{0}", message.toString());
        listener().onInstallationSuccessResponse(message);
    }

    @Override
    protected void onInstallationFailureResponse(final UpdateMessage message)
    throws UpdateMessageException {
        logger.log(Level.INFO, "Received installation failure response:\n{0}", message.toString());
        listener().onInstallationFailureResponse(message);
    }

    @Override
    protected void onUnsubscriptionSuccessResponse(final UpdateMessage message)
    throws UpdateMessageException {
        logger.log(Level.INFO, "Received unsubscription success response:\n{0}", message.toString());
        listener().onUnsubscriptionSuccessResponse(message);
    }

    @Override
    protected void onUnsubscriptionFailureResponse(final UpdateMessage message)
    throws UpdateMessageException {
        logger.log(Level.INFO, "Received unsubscription failure response:\n{0}", message.toString());
        listener().onUnsubscriptionFailureResponse(message);
    }

    private UpdateListener listener() {
        return null == parameters ? NULL : parameters.updateListener();
    }
}

/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.agent.impl.javaee;

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

    @Resource
    ConnectionFactory connectionFactory;

    @Resource(name = "TrueUpdate")
    Topic destination;

    @CheckForNull
    ApplicationParameters applicationParameters;

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
            final ApplicationParameters applicationParameters) {
        if (anotherApplicationDescriptor(applicationParameters))
            throw new IllegalArgumentException(
                    "Cannot create an update agent for another application descriptor.");
        this.applicationParameters = applicationParameters;
        return this;
    }

    @Override public UpdateAgent build() { return new BasicUpdateAgent(this); }

    @Override
    protected void onSubscriptionSuccessResponse(UpdateMessage message)
    throws Exception {
        applicationListener().onSubscriptionSuccessResponse(log(message));
    }

    @Override
    protected void onSubscriptionFailureResponse(UpdateMessage message)
    throws Exception {
        applicationListener().onSubscriptionFailureResponse(log(message));
    }

    @Override
    protected void onUpdateNotice(UpdateMessage message)
    throws Exception {
        applicationListener().onUpdateNotice(log(message));
    }

    @Override
    protected void onInstallationSuccessResponse(UpdateMessage message)
    throws Exception {
        applicationListener().onInstallationSuccessResponse(log(message));
    }

    @Override
    protected void onInstallationFailureResponse(UpdateMessage message)
    throws Exception {
        applicationListener().onInstallationFailureResponse(log(message));
    }

    @Override
    protected void onUnsubscriptionSuccessResponse(UpdateMessage message)
    throws Exception {
        applicationListener().onUnsubscriptionSuccessResponse(log(message));
    }

    @Override
    protected void onUnsubscriptionFailureResponse(UpdateMessage message)
    throws Exception {
        applicationListener().onUnsubscriptionFailureResponse(log(message));
    }

    private boolean anotherApplicationDescriptor(
            final ApplicationParameters parameters) {
        final ApplicationDescriptor ad = applicationDescriptor();
        return null != ad && !ad.equals(parameters.applicationDescriptor());
    }

    private ApplicationListener applicationListener() {
        final ApplicationParameters ap = applicationParameters;
        assert null != ap : "The filter should not have accepted this update message.";
        return ap.applicationListener();
    }

    private @CheckForNull ApplicationDescriptor applicationDescriptor() {
        final ApplicationParameters ap = applicationParameters;
        return null == ap ? null : ap.applicationDescriptor();
    }

    private static UpdateMessage log(final UpdateMessage message) {
        logger.log(Level.FINE, "Update message:\n{0}", message);
        return message;
    }
}

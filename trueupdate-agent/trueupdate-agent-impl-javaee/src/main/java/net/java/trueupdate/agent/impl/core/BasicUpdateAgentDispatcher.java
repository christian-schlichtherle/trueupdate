/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.agent.impl.core;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.*;
import javax.annotation.concurrent.NotThreadSafe;
import net.java.trueupdate.agent.spec.*;
import net.java.trueupdate.manager.spec.*;

/**
 * A basic update agent dispatcher.
 *
 * @author Christian Schlichtherle
 */
@NotThreadSafe
@SuppressWarnings("ProtectedField")
public abstract class BasicUpdateAgentDispatcher
extends UpdateMessageDispatcher {

    private static final Logger
            logger = Logger.getLogger(BasicUpdateAgentDispatcher.class.getName());

    private static final ApplicationListener NULL = new ApplicationListener();

    private final Map<ApplicationDescriptor, ApplicationListener>
            applicationListeners = new HashMap<>();

    public void subscribe(ApplicationParameters parameters) {
        applicationListeners.put(parameters.applicationDescriptor(),
                                 parameters.applicationListener());
    }

    public void unsubscribe(ApplicationParameters parameters) {
        applicationListeners.remove(parameters.applicationDescriptor());
    }

    @Override
    protected void onSubscriptionSuccessResponse(UpdateMessage message)
    throws Exception {
        applicationListener(message).onSubscriptionSuccessResponse(log(message));
    }

    @Override
    protected void onSubscriptionFailureResponse(UpdateMessage message)
    throws Exception {
        applicationListener(message).onSubscriptionFailureResponse(log(message));
    }

    @Override
    protected void onUpdateNotice(UpdateMessage message)
    throws Exception {
        applicationListener(message).onUpdateNotice(log(message));
    }

    @Override
    protected void onInstallationSuccessResponse(UpdateMessage message)
    throws Exception {
        applicationListener(message).onInstallationSuccessResponse(log(message));
    }

    @Override
    protected void onInstallationFailureResponse(UpdateMessage message)
    throws Exception {
        applicationListener(message).onInstallationFailureResponse(log(message));
    }

    @Override
    protected void onUnsubscriptionSuccessResponse(UpdateMessage message)
    throws Exception {
        applicationListener(message).onUnsubscriptionSuccessResponse(log(message));
    }

    @Override
    protected void onUnsubscriptionFailureResponse(UpdateMessage message)
    throws Exception {
        applicationListener(message).onUnsubscriptionFailureResponse(log(message));
    }

    private ApplicationListener applicationListener(UpdateMessage message) {
        return applicationListener(message.applicationDescriptor());
    }

    private ApplicationListener applicationListener(
            final ApplicationDescriptor descriptor) {
        final ApplicationListener al = applicationListeners.get(descriptor);
        return null != al ? al : NULL;
    }

    private static UpdateMessage log(final UpdateMessage message) {
        logger.log(Level.FINE, "Update message:\n{0}", message);
        return message;
    }
}

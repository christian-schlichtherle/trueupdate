/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.agent.core;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.*;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.*;
import net.java.trueupdate.agent.spec.*;
import net.java.trueupdate.manager.spec.*;


/**
 * A basic update agent dispatcher.
 *
 * @author Christian Schlichtherle
 */
@NotThreadSafe
@SuppressWarnings("ProtectedField")
public abstract class BasicUpdateMessageDispatcher
extends BasicUpdateMessageListener implements UpdateMessageDispatcher {

    private static final Logger
            logger = Logger.getLogger(BasicUpdateMessageDispatcher.class.getName());

    private final Map<ApplicationDescriptor, ApplicationAccount>
            applicationAccounts = new HashMap<>();

    /** Returns the capacity of the queue to use for update messages. */
    protected int capacity() { return 100; }

    /**
     * Subscribes to update messages.
     *
     * @param parameters the application parameters.
     */
    @Override
    public void subscribe(ApplicationParameters parameters) {
        applicationAccount(parameters.applicationDescriptor())
                .applicationListener(parameters.applicationListener());
    }

    private ApplicationAccount applicationAccount(
            final ApplicationDescriptor descriptor) {
        ApplicationAccount aa = applicationAccounts.get(descriptor);
        if (null == aa) {
            aa = new ApplicationAccount(descriptor, capacity());
            applicationAccounts.put(descriptor, aa);
        }
        return aa;
    }

    /**
     * Unsubscribes from update messages.
     *
     * @param parameters the application parameters.
     */
    @Override
    public void unsubscribe(ApplicationParameters parameters) {
        applicationAccounts.remove(parameters.applicationDescriptor());
    }

    @Override
    public void onUpdateMessage(final UpdateMessage message) throws Exception {
        final ApplicationDescriptor ad = message.applicationDescriptor();
        final ApplicationAccount aa = applicationAccount(ad);
        aa.enqueue(message);
        if (null != aa.applicationListener()) {
            for (UpdateMessage polled; null != (polled = aa.poll()); )
                super.onUpdateMessage(polled);
        } else {
            logger.log(Level.FINE,
                    "Stored update message from update manager in volatile queue because there is no registered listener for the described application:\n{0}",
                    message);
        }
    }

    @Override
    protected void onSubscriptionSuccessResponse(UpdateMessage message)
    throws Exception {
        applicationListener(message).onSubscriptionSuccessResponse(message);
    }

    @Override
    protected void onSubscriptionFailureResponse(UpdateMessage message)
    throws Exception {
        applicationListener(message).onSubscriptionFailureResponse(message);
    }

    @Override
    protected void onUpdateNotice(UpdateMessage message)
    throws Exception {
        applicationListener(message).onUpdateNotice(message);
    }

    @Override
    protected void onInstallationSuccessResponse(UpdateMessage message)
    throws Exception {
        applicationListener(message).onInstallationSuccessResponse(message);
    }

    @Override
    protected void onInstallationFailureResponse(UpdateMessage message)
    throws Exception {
        applicationListener(message).onInstallationFailureResponse(message);
    }

    @Override
    protected void onUnsubscriptionSuccessResponse(UpdateMessage message)
    throws Exception {
        applicationListener(message).onUnsubscriptionSuccessResponse(message);
    }

    @Override
    protected void onUnsubscriptionFailureResponse(UpdateMessage message)
    throws Exception {
        applicationListener(message).onUnsubscriptionFailureResponse(message);
    }

    private ApplicationListener applicationListener(UpdateMessage message) {
        return applicationAccount(message.applicationDescriptor())
                .applicationListener();
    }
}
@Immutable
final class ApplicationAccount {

    private final ApplicationDescriptor applicationDescriptor;
    private final Queue<UpdateMessage> updateMessages;
    private @CheckForNull ApplicationListener applicationListener;

    public ApplicationAccount(
            final ApplicationDescriptor applicationDescriptor,
            final int capacity) {
        assert null != applicationDescriptor;
        this.applicationDescriptor = applicationDescriptor;
        if (0 >= capacity) throw new IllegalArgumentException();
        this.updateMessages = new LinkedBlockingQueue<>(capacity);
    }

    ApplicationDescriptor applicationDescriptor() {
        return applicationDescriptor;
    }

    @Nullable ApplicationListener applicationListener() {
        return applicationListener;
    }

    void applicationListener(final ApplicationListener applicationListener) {
        assert null != applicationListener;
        this.applicationListener = applicationListener;
    }

    void enqueue(final UpdateMessage message) {
        assert applicationDescriptor().equals(message.applicationDescriptor());
        while (!updateMessages.offer(message)) updateMessages.remove();
    }

    @CheckForNull UpdateMessage poll() { return updateMessages.poll(); }
}

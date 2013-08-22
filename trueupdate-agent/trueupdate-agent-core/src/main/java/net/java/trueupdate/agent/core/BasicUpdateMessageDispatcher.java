/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.agent.core;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.*;
import javax.annotation.*;
import javax.annotation.concurrent.*;
import net.java.trueupdate.agent.spec.*;
import net.java.trueupdate.manager.spec.*;

/**
 * A basic update agent dispatcher.
 *
 * @author Christian Schlichtherle
 */
@NotThreadSafe
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
        account(parameters.applicationDescriptor())
                .listener(parameters.applicationListener());
    }

    private ApplicationAccount account(final ApplicationDescriptor descriptor) {
        ApplicationAccount account = applicationAccounts.get(descriptor);
        if (null == account) {
            account = new ApplicationAccount(descriptor, capacity());
            applicationAccounts.put(descriptor, account);
        }
        return account;
    }

    /**
     * Unsubscribes from update messages.
     *
     * @param parameters the application parameters.
     */
    @Override
    public void unsubscribe(ApplicationParameters parameters) {
        final ApplicationAccount account =
                applicationAccounts.remove(parameters.applicationDescriptor());
        if (null != account) {
            final int size = account.size();
            if (0 != size)
                logger.log(Level.FINE,
                        "Discarding {0} undelivered updates messages because the addressed application has unsubscribed.",
                        size);
        }
    }

    @Override
    public void onUpdateMessage(final UpdateMessage message) throws Exception {
        final ApplicationDescriptor descriptor = message.applicationDescriptor();
        final ApplicationAccount account = account(descriptor);
        account.enqueue(message);
        if (null != account.listener()) {
            for (UpdateMessage polled; null != (polled = account.poll()); )
                super.onUpdateMessage(polled);
        } else {
            logger.log(Level.FINE,
                    "Stored update message from update manager in volatile queue because there is no registered listener for the addressed application:\n{0}",
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
        return account(message.applicationDescriptor())
                .listener();
    }
}

final class ApplicationAccount {

    private final ApplicationDescriptor descriptor;
    private final Queue<UpdateMessage> queue;
    private @CheckForNull ApplicationListener listener;

    public ApplicationAccount(
            final ApplicationDescriptor descriptor,
            final int capacity) {
        assert null != descriptor;
        this.descriptor = descriptor;
        if (0 >= capacity) throw new IllegalArgumentException();
        this.queue = new LinkedBlockingQueue<>(capacity);
    }

    ApplicationDescriptor descriptor() { return descriptor; }

    @Nullable ApplicationListener listener() { return listener; }

    void listener(final ApplicationListener listener) {
        assert null != listener;
        this.listener = listener;
    }

    int size() { return queue.size(); }

    void enqueue(final UpdateMessage message) {
        assert descriptor().equals(message.applicationDescriptor());
        while (!queue.offer(message)) queue.remove();
    }

    @CheckForNull UpdateMessage poll() { return queue.poll(); }
}

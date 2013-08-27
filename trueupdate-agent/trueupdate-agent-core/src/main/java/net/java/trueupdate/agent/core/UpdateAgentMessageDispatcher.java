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
 * An update message dispatcher.
 *
 * @author Christian Schlichtherle
 */
@NotThreadSafe
public class UpdateAgentMessageDispatcher extends UpdateMessageListener {

    private static final Logger
            logger = Logger.getLogger(UpdateAgentMessageDispatcher.class.getName());

    private final Map<ApplicationDescriptor, ApplicationAccount>
            accounts = new HashMap<ApplicationDescriptor, ApplicationAccount>();

    /** Returns the capacity of the queue to use for update messages. */
    protected int capacity() { return 100; }

    /**
     * Subscribes to update messages for the application descriptor by using
     * the application listener in the given application parameters.
     *
     * @param parameters the application parameters.
     */
    public void subscribe(ApplicationParameters parameters) {
        account(parameters.applicationDescriptor())
                .listener(parameters.applicationListener());
    }

    private ApplicationAccount account(final ApplicationDescriptor descriptor) {
        ApplicationAccount account = accounts.get(descriptor);
        if (null == account) {
            account = new ApplicationAccount(capacity());
            accounts.put(descriptor, account);
        }
        return account;
    }

    /**
     * Unsubscribes from update messages for the application descriptor in the
     * given application parameters.
     *
     * @param parameters the application parameters.
     */
    public void unsubscribe(ApplicationParameters parameters) {
        final ApplicationAccount account =
                accounts.remove(parameters.applicationDescriptor());
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
        return account(message.applicationDescriptor()).listener();
    }
}

final class ApplicationAccount {

    private final Queue<UpdateMessage> queue;
    private @CheckForNull ApplicationListener listener;

    public ApplicationAccount(
            final int capacity) {
        if (0 >= capacity) throw new IllegalArgumentException();
        this.queue = new LinkedBlockingQueue<UpdateMessage>(capacity);
    }

    @Nullable ApplicationListener listener() { return listener; }

    void listener(final ApplicationListener listener) {
        assert null != listener;
        this.listener = listener;
    }

    int size() { return queue.size(); }

    void enqueue(final UpdateMessage message) {
        while (!queue.offer(message)) queue.remove();
    }

    @CheckForNull UpdateMessage poll() { return queue.poll(); }
}

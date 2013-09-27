/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.agent.jms;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.annotation.concurrent.Immutable;
import net.java.trueupdate.jms.*;

/**
 * A context for the JMS Update Agent.
 *
 * @author Christian Schlichtherle
 */
@Immutable
public final class JmsUpdateAgentContext {

    private final JmsUpdateAgent agent;
    private final JmsReceiver receiver;

    public JmsUpdateAgentContext() {
        this(JmsUpdateAgentParameters.load());
    }

    public JmsUpdateAgentContext(final JmsUpdateAgentParameters parameters) {
        agent = new JmsUpdateAgent(parameters);
        final MessagingParameters mp = parameters.messaging();
        // The maximum pool size is one in order to prevent messages to be
        // processed out of their sequence order.
        final ExecutorService executorService = new ThreadPoolExecutor(
                0, 1,
                60, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(),
                JmsReceiver.LISTENER_THREAD_FACTORY);
        receiver = JmsReceiver
                .builder()
                .connectionFactory(mp.connectionFactory())
                .destination(mp.fromDestination())
                .subscriptionName(mp.fromName())
                .messageSelector("manager = false")
                .updateMessageListener(agent)
                .executorService(executorService)
                .build();
    }

    public void start() throws Exception {
        new Thread(receiver, "TrueUpdate Agent JMS / Receiver Thread").start();
        agent.subscribe();
    }

    public void stop(final long timeout, final TimeUnit unit) throws Exception {
        // HC SVNT DRACONIS
        receiver.stop(timeout, unit);
        agent.unsubscribe();
    }
}

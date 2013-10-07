/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.agent.jms;

import java.util.*;
import java.util.concurrent.*;
import java.util.logging.*;
import javax.annotation.concurrent.Immutable;
import net.java.trueupdate.agent.core.TimerParameters;
import net.java.trueupdate.jms.*;

/**
 * A context for the JMS Update Agent.
 *
 * @author Christian Schlichtherle
 */
@Immutable
public final class JmsUpdateAgentContext {

    private final TimerParameters subscriptionDelay;
    private final JmsUpdateAgent agent;
    private final JmsReceiver receiver;

    public JmsUpdateAgentContext() {
        this(JmsUpdateAgentParameters.load());
    }

    public JmsUpdateAgentContext(final JmsUpdateAgentParameters parameters) {
        subscriptionDelay = parameters.subscriptionTimer();
        agent = new JmsUpdateAgent(parameters);
        final JmsParameters jp = parameters.messaging();
        // The maximum pool size is one in order to prevent messages to be
        // processed out of their sequence order.
        final ExecutorService es = new ThreadPoolExecutor(
                0, 1,
                60, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(),
                JmsReceiver.LISTENER_THREAD_FACTORY);
        receiver = JmsReceiver
                .builder()
                .connectionFactory(jp.connectionFactory())
                .destination(jp.fromDestination())
                .subscriptionName(jp.fromName())
                .messageSelector("manager = false")
                .updateMessageListener(agent)
                .executorService(es)
                .build();
    }

    public void start() throws Exception {
        new Thread(receiver, "TrueUpdate Agent JMS / Receiver").start();
        final long delay = subscriptionDelay.unit()
                .toMillis(subscriptionDelay.delay());
        if (0 < delay) {
            final Timer timer = new Timer(
                    "TrueUpdate Agent JMS / Subscription Timer", true);
            timer.schedule(new TimerTask() {
                @Override public void run() {
                    try {
                        agent.subscribe();
                    } catch (Exception ex) {
                        Logger  .getLogger(JmsUpdateAgentContext.class.getName())
                                .log(Level.WARNING,
                                    "Could not subscribe to update agent:", ex);
                    }
                }
            }, delay);
        } else {
            agent.subscribe();
        }
    }

    public void stop(final long timeout, final TimeUnit unit) throws Exception {
        // HC SVNT DRACONIS
        receiver.stop(timeout, unit);
        agent.unsubscribe();
    }
}

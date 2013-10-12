/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.agent.jms;

import java.util.*;
import java.util.concurrent.*;
import java.util.logging.*;
import net.java.trueupdate.agent.core.*;
import net.java.trueupdate.jms.*;

/**
 * Starts and stops a JMS based update agent.
 *
 * @author Christian Schlichtherle
 */
public final class JmsUpdateAgentController
implements UpdateAgentController {

    private final JmsUpdateAgentParameters parameters;
    private final JmsUpdateAgent agent;
    private final JmsReceiver receiver;
    private boolean started;

    public JmsUpdateAgentController() {
        this(JmsUpdateAgentParameters.load());
    }

    public JmsUpdateAgentController(
            final JmsUpdateAgentParameters parameters) {
        // HC SVNT DRACONIS
        this.parameters = parameters;
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

    @Override public void start() {
        if (started) return;
        try {
            start0();
        } catch (Exception ex) {
            throw new IllegalStateException(
                    "Failed to start the update agent.", ex);
        }
        started = true;
    }

    private void start0() throws Exception {
        // HC SVNT DRACONIS!
        new Thread(receiver, "TrueUpdate Agent JMS / Receiver").start();
        final TimerParameters tp = parameters.subscriptionTimer();
        final long delay = tp.unit().toMillis(tp.delay());
        if (0 < delay) {
            final Timer timer = new Timer(
                    "TrueUpdate Agent JMS / Subscription Timer", true);
            timer.schedule(new TimerTask() {
                @Override public void run() {
                    try {
                        agent.subscribe();
                    } catch (Exception ex) {
                        Logger  .getLogger(JmsUpdateAgentController.class.getName())
                                .log(Level.WARNING,
                                        "Could not subscribe to update manager:", ex);
                    }
                }
            }, delay);
        } else {
            agent.subscribe();
        }
    }

    @Override public void stop(final long timeout, final TimeUnit unit) {
        if (!started) return;
        try {
            stop0(10, TimeUnit.SECONDS);
        } catch (Exception ex) {
            throw new IllegalStateException(
                    "Failed to stop the update agent.", ex);
        }
    }

    private void stop0(final long timeout, final TimeUnit unit) throws Exception {
        // HC SVNT DRACONIS
        try {
            receiver.stop(timeout, unit);
        } finally {
            agent.unsubscribe();
        }
    }
}

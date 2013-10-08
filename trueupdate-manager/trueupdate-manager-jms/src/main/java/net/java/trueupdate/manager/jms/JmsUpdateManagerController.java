/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.jms;

import java.util.Locale;
import java.util.concurrent.*;
import java.util.logging.*;
import net.java.trueupdate.jms.*;
import net.java.trueupdate.manager.core.*;

/**
 * Starts and stops a JMS based update manager.
 *
 * @author Christian Schlichtherle
 */
public final class JmsUpdateManagerController
implements UpdateManagerController {

    private static final Logger logger = Logger.getLogger(
            JmsUpdateManagerController.class.getName());

    private final JmsUpdateManagerParameters parameters;
    private final JmsUpdateManager manager;
    private final ScheduledExecutorService timer;
    private final JmsReceiver receiver;
    private boolean started;

    public JmsUpdateManagerController() {
        this(JmsUpdateManagerParameters.load());
    }

    public JmsUpdateManagerController(
            final JmsUpdateManagerParameters parameters) {
        // HC SVNT DRACONIS
        this.parameters = parameters;
        manager = new JmsUpdateManager(parameters);
        final JmsParameters jp = parameters.messaging();
        final ExecutorService es = Executors.newCachedThreadPool(
                JmsReceiver.LISTENER_THREAD_FACTORY);
        receiver = JmsReceiver
                .builder()
                .connectionFactory(jp.connectionFactory())
                .destination(jp.fromDestination())
                .subscriptionName(jp.fromName())
                .messageSelector("manager = true")
                .updateMessageListener(manager)
                .executorService(es)
                .build();
        timer = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
            @Override public Thread newThread(Runnable r) {
                return new Thread(r, "TrueUpdate Manager JMS / Update Timer");
            }
        });

        final UpdateServiceParameters usp = parameters.updateService();
        logger.log(Level.CONFIG,
                "The base URI of the update service is {0} .", usp.uri());
        final TimerParameters tp = parameters.updateTimer();
        logger.log(Level.CONFIG,
                "The delay / period for checking for artifact updates is {0} / {1} {2}.",
                new Object[] { tp.delay(), tp.period(),
                        tp.unit().name().toLowerCase(Locale.ENGLISH) });
    }

    @Override public void start() {
        if (started) return;
        try {
            start0();
        } catch (Exception ex) {
            throw new IllegalStateException(
                    "Failed to start the update manager.", ex);
        }
        started = true;
    }

    private void start0() throws Exception {
        // HC SVNT DRACONIS!
        new Thread(receiver, "TrueUpdate Manager JMS / Receiver").start();
        final TimerParameters tp = parameters.updateTimer();
        timer.scheduleAtFixedRate(new CheckForUpdates(manager), tp.delay(),
                tp.period(), tp.unit());
    }

    @Override public void stop(final long timeout, final TimeUnit unit) {
        if (!started) return;
        try {
            stop0(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        } catch (Exception ex) {
            throw new IllegalStateException(
                    "Failed to stop the update manager.", ex);
        }
    }

    private void stop0(long timeout, TimeUnit unit) throws Exception {
        // HC SVNT DRACONIS!
        final long stop = System.currentTimeMillis() + unit.toMillis(timeout);
        unit = TimeUnit.MILLISECONDS;
        try {
            if (1 != timer.shutdownNow().size())
                throw new AssertionError();
            timeout = Math.max(1, stop - System.currentTimeMillis());
            if (!timer.awaitTermination(timeout, unit))
                throw new TimeoutException();
        } finally {
            try {
                timeout = Math.max(1, stop - System.currentTimeMillis());
                receiver.stop(timeout, unit);
            } finally {
                manager.close();
            }
        }
    }
}

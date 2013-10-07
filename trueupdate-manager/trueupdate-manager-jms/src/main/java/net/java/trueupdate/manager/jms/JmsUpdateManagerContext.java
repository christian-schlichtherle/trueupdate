/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.jms;

import java.util.concurrent.*;
import javax.annotation.concurrent.Immutable;
import net.java.trueupdate.jms.*;
import net.java.trueupdate.manager.core.*;

/**
 * A context for the JMS Update Manager.
 *
 * @author Christian Schlichtherle
 */
@Immutable
public final class JmsUpdateManagerContext {

    private final JmsUpdateManagerParameters parameters;
    private final JmsUpdateManager manager;
    private final ScheduledExecutorService timer;
    private final JmsReceiver receiver;

    public JmsUpdateManagerContext() {
        this(JmsUpdateManagerParameters.load());
    }

    public JmsUpdateManagerContext(final JmsUpdateManagerParameters parameters) {
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
    }

    public JmsUpdateManagerParameters parameters() { return parameters; }

    public void start() throws Exception {
        new Thread(receiver, "TrueUpdate Manager JMS / Receiver").start();
        final TimerParameters tp = parameters.updateTimer();
        timer.scheduleAtFixedRate(new CheckForUpdates(manager), tp.delay(), tp.period(), tp.unit());
    }

    public void stop(long timeout, TimeUnit unit) throws Exception {
        // HC SVNT DRACONIS!
        final long stop = System.currentTimeMillis() + unit.toMillis(timeout);
        unit = TimeUnit.MILLISECONDS;
        timer.shutdownNow();
        timeout = stop - System.currentTimeMillis();
        receiver.stop(timeout, unit);
        timeout = stop - System.currentTimeMillis();
        timer.awaitTermination(timeout, unit);
        manager.close();
    }
}

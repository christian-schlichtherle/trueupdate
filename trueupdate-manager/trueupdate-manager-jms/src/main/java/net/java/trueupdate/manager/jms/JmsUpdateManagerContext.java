/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.jms;

import java.util.concurrent.*;
import javax.annotation.concurrent.Immutable;
import net.java.trueupdate.jms.*;
import net.java.trueupdate.manager.core.CheckForUpdates;
import net.java.trueupdate.manager.spec.TimerParameters;

/**
 * A context for the JMS Update Manager.
 *
 * @author Christian Schlichtherle
 */
@Immutable
public final class JmsUpdateManagerContext {

    private static final ExecutorService executorService =
            Executors.newCachedThreadPool(JmsReceiver.LISTENER_THREAD_FACTORY);

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
        final MessagingParameters mp = parameters.messaging();
        receiver = JmsReceiver
                .builder()
                .connectionFactory(mp.connectionFactory())
                .destination(mp.fromDestination())
                .subscriptionName(mp.fromName())
                .messageSelector("manager = true")
                .updateMessageListener(manager)
                .executorService(executorService)
                .build();
        timer = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
            @Override public Thread newThread(Runnable r) {
                return new Thread(r, "TrueUpdate Manager JMS / Update Timer Thread");
            }
        });
    }

    public JmsUpdateManagerParameters parameters() { return parameters; }

    public void start() throws Exception {
        new Thread(receiver, "TrueUpdate Manager JMS / Receiver Thread").start();
        final TimerParameters tp = parameters.checkForUpdates();
        timer.scheduleAtFixedRate(new CheckForUpdates(manager), tp.delay(), tp.period(), tp.unit());
    }

    public void stop(final long timeout, final TimeUnit unit) throws Exception {
        // HC SVNT DRACONIS!
        final long stop = System.currentTimeMillis() + unit.toMillis(timeout);
        long remaining;
        timer.shutdownNow();
        remaining = stop - System.currentTimeMillis();
        receiver.stop(remaining, TimeUnit.MILLISECONDS);
        remaining = stop - System.currentTimeMillis();
        timer.awaitTermination(remaining, TimeUnit.MILLISECONDS);
        manager.close();
    }
}

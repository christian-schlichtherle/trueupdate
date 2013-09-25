/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.jms;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import javax.annotation.concurrent.Immutable;
import net.java.trueupdate.jms.*;
import net.java.trueupdate.manager.core.CheckForUpdates;

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
        final MessagingParameters mp = parameters.messagingParameters();
        receiver = JmsReceiver
                .builder()
                .connectionFactory(mp.connectionFactory())
                .destination(mp.fromDestination())
                .subscriptionName(mp.fromName())
                .messageSelector("manager = true")
                .updateMessageListener(manager)
                .build();
        timer = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
            @Override public Thread newThread(Runnable r) {
                return new Thread(r, "TrueUpdate Manager JMS / Timer Thread");
            }
        });
    }

    public JmsUpdateManagerParameters parameters() { return parameters; }

    public void start() throws Exception {
        new Thread(receiver, "TrueUpdate Manager JMS / Receiver Thread").start();
        final int period = parameters.checkUpdatesIntervalMinutes();
        timer.scheduleAtFixedRate(new CheckForUpdates(manager), period, period, TimeUnit.MINUTES);
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

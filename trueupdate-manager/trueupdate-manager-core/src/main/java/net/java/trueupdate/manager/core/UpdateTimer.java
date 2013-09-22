/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.core;

import net.java.trueupdate.manager.spec.UpdateManager;
import java.util.logging.*;
import javax.annotation.WillNotClose;
import javax.annotation.concurrent.ThreadSafe;
import net.java.trueupdate.util.Objects;

/**
 * A runnable which periodically checks for artifact updates.
 * Once started, this runnable continues until another thread calls
 * {@link #stop()}.
 *
 * @author Christian Schlichtherle
 */
@ThreadSafe
public final class UpdateTimer implements Runnable {

    private static final Logger
            logger = Logger.getLogger(UpdateTimer.class.getName());

    private final Object lock = new Object();
    private final UpdateManager updateManager;
    private final int checkUpdatesIntervalMinutes;
    private boolean closed;

    public UpdateTimer(
            final @WillNotClose UpdateManager updateManager,
            final int checkUpdatesIntervalMinutes) {
        this.updateManager = Objects.requireNonNull(updateManager);
        this.checkUpdatesIntervalMinutes = requirePositive(checkUpdatesIntervalMinutes);
    }

    private static int requirePositive(final int i) {
        if (0 >= i) throw new IllegalArgumentException();
        return i;
    }

    @Override public void run() {
        final long intervalMillis = checkUpdatesIntervalMinutes * 60L * 1000;
        long startedMillis = System.currentTimeMillis();
        synchronized (lock) {
            while (true) {
                try {
                    final long durationMillis = System.currentTimeMillis() - startedMillis;
                    final long sleepMillis = intervalMillis - durationMillis;
                    try {
                        lock.wait(Math.max(sleepMillis, 1));
                    } catch (InterruptedException wakeUpCall) {
                    }
                    startedMillis = System.currentTimeMillis();
                    if (closed) break;
                    updateManager.checkUpdates();
                } catch (Exception ex) {
                    logger.log(Level.SEVERE, "Checking for updates failed.", ex);
                }
            }
            lock.notifyAll();
        }
    }

    /**
     * Stops this runnable from a different thread.
     * When returning from this method, the client may safely assume that the
     * other thread is not executing the {@link #run()} method anymore.
     */
    public void stop() {
        synchronized (lock) {
            if (closed) return;
            closed = true;
            lock.notifyAll();
            while (true) try {
                lock.wait();
                break;
            } catch (InterruptedException dontStopTillYouDrop) {
            }
        }
    }
}

/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.servlets;

import java.util.logging.*;
import javax.annotation.concurrent.ThreadSafe;
import net.java.trueupdate.manager.core.UpdateManager;

/**
 * @author Christian Schlichtherle
 */
@ThreadSafe
final class UpdateTimer implements Runnable {

    private static final Logger
            logger = Logger.getLogger(UpdateTimer.class.getName());

    private final Object lock = new Object();
    private final UpdateManager updateManager;
    private final int checkUpdatesIntervalMinutes;
    private boolean closed;

    UpdateTimer(
            final UpdateManager updateManager,
            final int checkUpdatesIntervalMinutes) {
        assert null != updateManager;
        this.updateManager = updateManager;
        this.checkUpdatesIntervalMinutes = requirePositive(checkUpdatesIntervalMinutes);
    }

    private static int requirePositive(int n) {
        if (0 >= n) throw new IllegalArgumentException();
        return n;
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
    void stop() {
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

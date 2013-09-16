/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.mini;

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
        while (true) {
            try {
                final long durationMillis = System.currentTimeMillis() - startedMillis;
                final long sleepMillis = intervalMillis - durationMillis;
                if (0 < sleepMillis) Thread.sleep(sleepMillis);
                startedMillis = System.currentTimeMillis();
                synchronized (this) {
                    if (closed) break;
                    updateManager.checkUpdates();
                }
            } catch (InterruptedException ex) {
            } catch (Exception ex) {
                logger.log(Level.SEVERE, "Checking for updates failed.", ex);
            }
        }
    }

    /**
     * Stops this timer.
     * After the call to this method, the client may safely assume that this
     * timer will not call {@link UpdateManager#checkUpdates} anymore.
     */
    synchronized void stop() {
        closed = true;
    }
}

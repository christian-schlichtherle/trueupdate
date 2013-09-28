/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.core;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.concurrent.Immutable;
import net.java.trueupdate.manager.spec.UpdateManager;
import net.java.trueupdate.util.Objects;

/**
 * A runnable task which uses a given update manager to check for updates.
 * If the check fails with an exception, then a message gets logged.
 *
 * @author Christian Schlichtherle
 */
@Immutable
public final class CheckForUpdates implements Runnable {

    private static final Logger
            logger = Logger.getLogger(CheckForUpdates.class.getName());

    private final UpdateManager manager;

    public CheckForUpdates(final UpdateManager manager) {
        this.manager = Objects.requireNonNull(manager);
    }

    @Override public void run() {
        try {
            manager.checkForUpdates();
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Could not check for updates.", ex);
        }
    }
}

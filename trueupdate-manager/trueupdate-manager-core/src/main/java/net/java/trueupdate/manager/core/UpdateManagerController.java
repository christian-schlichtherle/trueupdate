/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.core;

import java.util.concurrent.*;

/**
 * Starts and stops an asynchronous update manager.
 *
 * @author Christian Schlichtherle
 */
public interface UpdateManagerController {

    /** Starts the update manager. */
    void start();

    /** Stops the update manager. */
    void stop(long timeout, TimeUnit unit);
}

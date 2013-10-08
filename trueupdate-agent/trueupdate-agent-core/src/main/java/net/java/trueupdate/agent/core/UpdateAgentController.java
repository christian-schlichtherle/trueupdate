/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.agent.core;

import java.util.concurrent.*;

/**
 * Starts and stops an asynchronous update agent.
 *
 * @author Christian Schlichtherle
 */
public interface UpdateAgentController {

    /** Starts the update agent. */
    void start();

    /** Stops the update agent. */
    void stop(long timeout, TimeUnit unit);
}

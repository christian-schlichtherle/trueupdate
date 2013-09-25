/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.spec;

import java.text.MessageFormat;
import java.util.logging.Level;
import net.java.trueupdate.message.UpdateMessage;

/**
 * A progress monitor.
 * <p>
 * Implementations must be thread-safe.
 * <p>
 * Applications have no need to implement this class and should not do so
 * because it may be subject to future expansion.
 *
 * @author Christian Schlichtherle
 */
public interface ProgressMonitor {

    /**
     * Returns {@code true} if and only if the given logging level is enabled.
     */
    boolean isLoggable(Level level);

    /**
     * Sends a progress notice to the update agent.
     *
     * @param level the logging level.
     * @param key the message format key.
     *            This is a key in the resource bundle for the
     *            {@link UpdateMessage} class which is used to look up the
     *            message format pattern.
     * @param parameters the parameters to the message format pattern.
     * @see   MessageFormat
     */
    void log(Level level, String key, Object... parameters);

    /**
     * Sends a redeployment request to the update agent and waits for a
     * response.
     * This handshake ensures that the update agent has processed all progress
     * notices before the redeployment is happening.
     */
    void aboutToRedeploy() throws Exception;
}

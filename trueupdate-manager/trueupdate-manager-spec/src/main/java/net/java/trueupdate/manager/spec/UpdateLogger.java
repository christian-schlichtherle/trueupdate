/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.spec;

import net.java.trueupdate.message.LogMessage.Level;

/**
 * An update logger.
 * <p>
 * Implementations must be thread-safe.
 * <p>
 * Applications have no need to implement this class and should not do so
 * because it may be subject to future expansion.
 *
 * @author Christian Schlichtherle
 */
public interface UpdateLogger {

    /**
     * Returns {@code true} if and only if the given logging level is enabled.
     */
    boolean isLoggable(Level level);

    /**
     * Logs a message with some parameters.
     *
     * @param level the message level.
     * @param code the message code, which is a key for the message catalog.
     *            This must refer to an entry in the resource bundle for the
     *            {@link net.java.trueupdate.message.LogMessage} class with the
     *            message format pattern to use.
     * @param args the message parameters.
     */
    void log(Level level, String code, Object... args);

    /**
     * Logs a message with an exception parameter.
     *
     * @param level the message level.
     * @param code the message code, which is a key for the message catalog.
     *            This must refer to an entry in the resource bundle for the
     *            {@link net.java.trueupdate.message.LogMessage} class with the
     *            message format pattern to use.
     * @param throwable the exception parameter.
     */
    //void log(Level level, String code, Throwable throwable);
}

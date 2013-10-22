/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.core;

import java.util.logging.ErrorManager;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * A log context maintains a map of inheritable thread local
 * {@linkplain LogChannel log channels}.
 *
 * @author Christian Schlichtherle
 */
final class LogContext extends Handler {

    private static final LogContext INSTANCE = new LogContext();
    static {
        Logger.getLogger("").addHandler(INSTANCE);
    }

    static void setChannel(LogChannel channel) { INSTANCE.set(channel); }

    static void resetChannel() { INSTANCE.reset(); }

    private ThreadLocal<LogChannel>
            channels = new InheritableThreadLocal<LogChannel>();

    private LogContext() { }

    @Override public void publish(final LogRecord record) {
        if (null == record || !isLoggable(record)) return;
        final LogChannel channel = channels.get();
        if (null == channel) return;
        // Prevent stack overflow in case the channel is logging, too.
        channels.set(null);
        try {
            channel.transmit(record);
        } catch (Exception ex) {
            reportError("Cannot transmit log record to update agent:", ex,
                    ErrorManager.WRITE_FAILURE);
        } finally {
            channels.set(channel);
        }
    }

    void set(LogChannel channel) { channels.set(channel); }

    void reset() { channels.remove(); }

    @Override public void flush() { }

    @Override public void close() { channels = null; }
}

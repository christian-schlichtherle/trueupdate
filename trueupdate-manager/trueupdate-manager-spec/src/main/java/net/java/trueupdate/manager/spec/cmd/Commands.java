/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.spec.cmd;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides functions for {@link Command}s.
 *
 * @author Christian Schlichtherle
 */
public final class Commands {

    private static final ThreadLocal<Boolean>
            busy = new InheritableThreadLocal<Boolean>();

    /**
     * Executes the given command.
     *
     * @throws IllegalStateException if this method is called recursively.
     */
    public static void execute(final Command cmd) throws Exception {
        if (Boolean.TRUE.equals(busy.get()))
            throw new UnsupportedOperationException(
                    "Nested commands are not supported because they can't get reverted when the enclosing command gets reverted - refactor to CompositeCommand instead.");
        busy.set(Boolean.TRUE);
        try {
            try {
                cmd.perform();
            } catch (final Throwable t1) {
                try {
                    cmd.revert();
                } catch (Throwable t2) {
                    logger().log(Level.SEVERE, "Exception while reverting command - the integrity of the system is probably corrupted.", t2);
                }
                rethrow(t1);
            }
        } finally {
            busy.remove();
        }
    }

    private static Logger logger() {
        return Logger.getLogger(Commands.class.getName());
    }

    private static void rethrow(final Throwable t1) throws Exception {
        try {
            throw t1;
        } catch (Error t2) {
            throw t2;
        } catch (Exception t2) {
            throw t2;
        } catch (Throwable t2) {
            throw new AssertionError(t2);
        }
    }

    /**
     * Decorates the given command with another command which logs the
     * duration of each command method using the given configuration.
     *
     * @return the logging command.
     */
    public static Command time(
            final Command cmd,
            final LogContext ctx) {
        return new Command() {

            @Override public void perform() throws Exception {
                time(LogContext.Method.perform);
            }

            @Override public void revert() throws Exception {
                time(LogContext.Method.revert);
            }

            void time(final LogContext.Method method) throws Exception {
                ctx.logStarting(method);
                Throwable t1 = null;
                final long started = System.currentTimeMillis();
                try { method.invoke(cmd); } catch (Throwable t2) { t1 = t2; }
                final long finished = System.currentTimeMillis();
                final long durationMillis = finished - started;
                if (null == t1) {
                    ctx.logSucceeded(method, durationMillis);
                } else {
                    ctx.logFailed(method, durationMillis);
                    rethrow(t1);
                }
            }
        };
    }

    /**
     * Decorates the given command with another command to ensure that the
     * {@link Command#revert} method of the given command is only called if a
     * previous call to the {@link Command#perform} method has succeeded, for
     * example if a subsequent command in a {@link CompositeCommand} fails.
     */
    public static Command atomic(final Command cmd) {
        return new Command() {

            boolean performed;

            @Override public void perform() throws Exception {
                if (performed)
                    throw new IllegalStateException("Not idempotent.");
                cmd.perform();
                performed = true;
            }

            @Override public void revert() throws Exception {
                if (performed) {
                    cmd.revert();
                    performed = false;
                }
            }
        };
    }

    private Commands() { }

}

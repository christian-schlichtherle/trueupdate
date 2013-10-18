/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.spec.tx;

import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides functions for {@link Transaction}s.
 *
 * @author Christian Schlichtherle
 */
public class Transactions {

    private static final ThreadLocal<Boolean>
            inTx = new InheritableThreadLocal<Boolean>();

    /**
     * Executes the given transaction.
     *
     * @throws TransactionException if {@link Transaction#rollback()} or
     *         {@link Transaction#commit()} throw an exception.
     * @throws IllegalStateException if this method is called recursively.
     */
    public static void execute(final Transaction tx) throws Exception {
        if (Boolean.TRUE.equals(inTx.get()))
            throw new IllegalStateException(
                    "Nested transactions are not supported because they can't get rolled back once committed - refactor to CompositeTransaction instead.");
        inTx.set(Boolean.TRUE);
        try {
            tx.prepare();
            try {
                tx.perform();
            } catch (final Exception ex) {
                assert !(ex instanceof TransactionException);
                try {
                    tx.rollback();
                } catch (RuntimeException ex2) {
                    throw new TransactionException(ex2);
                }
                throw ex;
            }
            try {
                tx.commit();
            } catch (RuntimeException ex) {
                throw new TransactionException(ex);
            }
        } finally {
            inTx.remove();
        }
    }

    /** Returns a transaction which does nothing. */
    public static Transaction noOp() {
        return new NullTransaction();
    }

    private static class NullTransaction extends Transaction {
        @Override public void perform() throws Exception { }
        @Override public void rollback() { }
    } // NullTransaction

    /**
     * Wraps the named transaction in another transaction which logs the
     * duration of each transaction method using the given configuration.
     *
     * @return the logging transaction.
     */
    public static Transaction time(final String message,
                                   final Transaction tx,
                                   final LoggerConfig config) {

        class Time extends Transaction {

            @Override public void prepare() throws Exception {
                time(Method.prepare, new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        tx.prepare();
                        return null;
                    }
                });
            }

            @Override public void perform() throws Exception {
                time(Method.perform, new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        tx.perform();
                        return null;
                    }
                });
            }

            @Override public void rollback() {
                timeUnchecked(Method.rollback, new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        tx.rollback();
                        return null;
                    }
                });
            }

            @Override public void commit() {
                timeUnchecked(Method.commit, new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        tx.commit();
                        return null;
                    }
                });
            }

            void timeUnchecked(final Method method, final Callable<Void> task) {
                try { time(method, task); }
                catch (RuntimeException ex) { throw ex; }
                catch (Exception ex){ throw new AssertionError(ex); }
            }

            void time(final Method method, final Callable<Void> task)
            throws Exception {
                Exception ex = null;
                final long started = System.currentTimeMillis();
                try { task.call(); } catch (Exception ex2) { ex = ex2; }
                final long finished = System.currentTimeMillis();
                final Logger logger = config.logger();
                final boolean succeeded = null == ex;
                final Level level = config.level(method, succeeded);
                if (logger.isLoggable(level)) {
                    final long duration = finished - started;
                    final long millis = duration % 1000;
                    final long seconds = duration / 1000 % 60;
                    final long minutes = duration / 1000 / 60 % 60;
                    final long hours = duration / 1000 / 60 / 60;
                    logger.log(level, message, new Object[] {
                        succeeded ? 0 : 1, method.ordinal(),
                        hours, minutes, seconds, millis });
                }
                if (!succeeded) throw ex;
            }
        } // Time

        return new Time();
    }

    private Transactions() { }

    public enum Method {
        prepare {
            @Override Level succeeded() { return Level.FINE; }
        },

        perform, rollback,

        commit {
            @Override Level succeeded() { return Level.FINE; }
        };

        Level succeeded() { return Level.INFO; }
        Level failed() { return Level.SEVERE; }
    }

    public static abstract class LoggerConfig {

        public abstract Logger logger();

        public Level level(Method method, boolean succeeded) {
            return succeeded ? method.succeeded() : method.failed();
        }
    }
}

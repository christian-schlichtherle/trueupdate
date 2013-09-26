/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.installer.core.tx;

import java.util.concurrent.Callable;
import net.java.trueupdate.message.LogMessage.Level;
import net.java.trueupdate.manager.spec.UpdateLogger;

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
            throw new IllegalStateException("Nested transactions are not supported because they can't get rolled back once committed - refactor to CompositeTransaction instead.");
        inTx.set(Boolean.TRUE);
        try {
            tx.prepare();
            try {
                tx.perform();
            } catch (final Exception ex) {
                assert !(ex instanceof TransactionException);
                try {
                    tx.rollback();
                } catch (final Exception ex2) {
                    assert !(ex2 instanceof TransactionException);
                    throw new TransactionException(ex2);
                }
                throw ex;
            }
            try {
                tx.commit();
            } catch (final Exception ex) {
                assert !(ex instanceof TransactionException);
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
        @Override public void rollback() throws Exception { }
    } // NullTransaction

    /**
     * Wraps the named transaction in another transaction which logs the
     * duration of each transaction method using the given configuration.
     *
     * @return the logging transaction.
     */
    public static Transaction timed(final String key,
                                    final Transaction tx,
                                    final LoggerConfig config) {

        class TimedTransaction extends Transaction {

            @Override public void prepare() throws Exception {
                time(Method.prepare, new Callable<Void>() {
                    @Override public Void call() throws Exception {
                        tx.prepare();
                        return null;
                    }
                });
            }

            @Override public void perform() throws Exception {
                time(Method.perform, new Callable<Void>() {
                    @Override public Void call() throws Exception {
                        tx.perform();
                        return null;
                    }
                });
            }

            @Override public void rollback() throws Exception {
                time(Method.rollback, new Callable<Void>() {
                    @Override public Void call() throws Exception {
                        tx.rollback();
                        return null;
                    }
                });
            }

            @Override public void commit() throws Exception {
                time(Method.commit, new Callable<Void>() {
                    @Override public Void call() throws Exception {
                        tx.commit();
                        return null;
                    }
                });
            }

            void time(final Method method, final Callable<Void> task)
            throws Exception {
                Exception ex = null;
                final long started = System.currentTimeMillis();
                try { task.call(); } catch (Exception ex2) { ex = ex2; }
                final long finished = System.currentTimeMillis();
                final UpdateLogger logger = config.logger();
                final boolean succeeded = null == ex;
                final Level level = config.level(method, succeeded);
                if (logger.isLoggable(level)) {
                    final long duration = finished - started;
                    final long millis = duration % 1000;
                    final long seconds = duration / 1000 % 60;
                    final long minutes = duration / 1000 / 60 % 60;
                    final long hours = duration / 1000 / 60 / 60;
                    logger.log(level, key, succeeded ? 0 : 1, method.ordinal(),
                            hours, minutes, seconds, millis);
                }
                if (!succeeded) throw ex;
            }

        } // TimedTransaction

        return new TimedTransaction();
    }

    private Transactions() { }

    public enum Method {
        prepare {
            @Override Level succeeded() { return Level.FINEST; }
        },

        perform, rollback,

        commit {
            @Override Level succeeded() { return Level.FINER; }
        };

        Level succeeded() { return Level.FINE; }
        Level failed() { return Level.WARNING; }
    }

    public static abstract class LoggerConfig {

        public abstract UpdateLogger logger();

        public Level level(Method method, boolean succeeded) {
            return succeeded ? method.succeeded() : method.failed();
        }
    }
}

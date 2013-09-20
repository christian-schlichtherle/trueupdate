/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.installer.core.tx;

import java.util.concurrent.Callable;
import java.util.logging.*;

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
    public static Transaction timed(final String name,
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
                try { task.call(); }
                catch (Exception ex2) { ex = ex2; }
                final long finished = System.currentTimeMillis();
                final Logger logger = config.logger();
                final Level level = config.level(method, null == ex);
                if (logger.isLoggable(level)) {
                    final float duration = (finished - started) / 1000.0f;
                    logger.log(level,
                            "{0} to {1} the {2} in {3} seconds.",
                            new Object[]{
                                    null == ex ? "Succeeded" : "Failed",
                                    method.name(), name, duration
                            });
                }
                if (null != ex) throw ex;
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

        public abstract Logger logger();

        public Level level(Method method, boolean succeeded) {
            return succeeded ? method.succeeded() : method.failed();
        }
    }
}

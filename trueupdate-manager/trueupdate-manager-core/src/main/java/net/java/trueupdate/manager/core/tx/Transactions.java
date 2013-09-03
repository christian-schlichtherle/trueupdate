/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.core.tx;

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

    private Transactions() { }
}

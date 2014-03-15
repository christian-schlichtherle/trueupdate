/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.spec.tx;

/**
 * A composite transaction.
 *
 * @author Christian Schlichtherle
 */
public final class CompositeTransaction extends Transaction {

    private final Transaction[] txs;
    private int index;

    public CompositeTransaction(final Transaction... txs) {
        if (0 >= txs.length) throw new IllegalArgumentException();
        this.txs = txs.clone();
    }

    @Override public void prepare() throws Exception {
        if (0 != index) throw new IllegalStateException();
    }

    @Override public void perform() throws Exception {
        while (index < txs.length) {
            final Transaction tx = txs[index];
            tx.prepare();
            index++;
            tx.perform();
        }
    }

    @Override public void commit() throws Exception {
        int i = index;
        while (0 < i) txs[--i].commit();
        index = 0;
    }

    @Override public void rollback() throws Exception {
        int i = index;
        while (0 < i) txs[--i].rollback();
        index = 0;
    }
}

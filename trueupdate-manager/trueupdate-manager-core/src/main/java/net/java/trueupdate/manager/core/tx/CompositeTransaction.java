/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.core.tx;

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

    @Override protected void prepare() throws Exception {
        if (0 != index) throw new IllegalStateException();
    }

    @Override protected void perform() throws Exception {
        while (index < txs.length) {
            final Transaction tx = txs[index];
            tx.prepare();
            index++;
            tx.perform();
        }
    }

    @Override protected void rollback() throws Exception {
        while (0 < index) txs[--index].rollback();
    }

    @Override protected void commit() throws Exception {
        while (0 < index) txs[--index].commit();
    }
}

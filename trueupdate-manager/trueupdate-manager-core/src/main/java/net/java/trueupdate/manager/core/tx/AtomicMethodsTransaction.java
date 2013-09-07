/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.core.tx;

/**
 * A local transaction which is made of truly atomic methods, that is its
 * methods either completely succeed or completely fail.
 * On this precondition, this class then cooperates with
 * {@link Transactions#execute} to make sure that its {@link #rollbackAtomic}
 * method is only called if {@link #performAtomic()} has succeeded before,
 * e.g. when composed into a {@link CompositeTransaction}.
 *
 * @see Transactions#execute
 * @see CompositeTransaction
 * @author Christian Schlichtherle
 */
public abstract class AtomicMethodsTransaction extends Transaction {

    private boolean performed;

    @Override protected final void prepare() throws Exception {
        if (performed) throw new IllegalStateException();
        prepareAtomic();
    }

    @Override protected final void perform() throws Exception {
        performAtomic();
        performed = true;
    }

    @Override protected final void rollback() throws Exception {
        if (performed) {
            rollbackAtomic();
            performed = false;
        }
    }

    @Override protected final void commit() throws Exception {
        commitAtomic();
        performed = false;
    }

    /**
     * Semantically identical to {@link #prepare}, but has to be truly atomic,
     * that is it either completely succeeds or completely fails.
     */
    protected void prepareAtomic() throws Exception { }

    /**
     * Semantically identical to {@link #perform}, but has to be truly atomic,
     * that is it either completely succeeds or completely fails.
     */
    protected abstract void performAtomic() throws Exception;

    /**
     * Semantically identical to {@link #rollback}, but has to be truly atomic,
     * that is it either completely succeeds or completely fails.
     */
    protected abstract void rollbackAtomic() throws Exception;

    /**
     * Semantically identical to {@link #commit}, but has to be truly atomic,
     * that is it either completely succeeds or completely fails.
     */
    protected void commitAtomic() throws Exception { }
}

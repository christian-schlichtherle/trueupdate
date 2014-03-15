/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.spec.tx;

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

    @Override public final void prepare() throws Exception {
        if (performed) throw new IllegalStateException();
        prepareAtomic();
    }

    @Override public final void perform() throws Exception {
        performAtomic();
        performed = true;
    }

    @Override public final void commit() throws Exception {
        commitAtomic();
        // performed = false; // don't be idempotent!
    }

    @Override public final void rollback() throws Exception {
        if (performed) {
            rollbackAtomic();
            performed = false;
        }
    }

    /**
     * Semantically identical to {@link #prepare}, but has to be truly atomic,
     * that is it either completely succeeds or completely fails.
     */
    public void prepareAtomic() throws Exception { }

    /**
     * Semantically identical to {@link #perform}, but has to be truly atomic,
     * that is it either completely succeeds or completely fails.
     */
    public abstract void performAtomic() throws Exception;

    /**
     * Semantically identical to {@link #commit}, but has to be truly atomic,
     * that is it either completely succeeds or completely fails.
     */
    public void commitAtomic() throws Exception { }

    /**
     * Semantically identical to {@link #rollback}, but has to be truly atomic,
     * that is it either completely succeeds or completely fails.
     */
    public abstract void rollbackAtomic() throws Exception;
}

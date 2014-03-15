/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.spec.tx;

/**
 * A simple interface for local transactions.
 * The use case is to compose file system operations with ACID properties into
 * larger transactions, for example for undeploying, updating and deploying an
 * enterprise application to an application server.
 * <p>
 * Note that transactions are generally not idempotent, that is once their
 * execution has succeeded, you may not be able to execute them successfully
 * again because the preconditions for a successful execution are no longer met.
 * However, transactions must be retryable, that is if {@link #prepare()} has
 * thrown an exception or {@link #perform()} has thrown an exception and
 * {@link #rollback()} has succeeded, then clients will still be able to
 * execute them successfully as soon as the preconditions for a successful
 * execution are met.
 *
 * @see Transactions#execute
 * @see CompositeTransaction
 * @author Christian Schlichtherle
 */
public abstract class Transaction {

    /**
     * Sets up this transaction for execution.
     * If this method throws an {@link Exception}, then this transaction gets
     * aborted <em>without</em>  calling {@link #rollback}, so it must not
     * leave any visible side effects.
     * If this method throws an {@link Exception} and this transaction is part
     * of a composite transaction, then the preceding transactions get
     * properly rolled back.
     */
    public void prepare() throws Exception { }

    /**
     * Executes the body of this transaction.
     * If this method throws an {@link Exception}, then this transaction gets
     * aborted <em>with</em> calling {@link #rollback}, so it may leave some
     * visible side effects.
     * If this method throws an {@link Exception} and this transaction is part
     * of a composite transaction, then the preceding transactions get
     * properly rolled back.
     */
    public abstract void perform() throws Exception;

    /**
     * Commits the visible side effects of the body of this transaction.
     * If this method succeeds, any visible side effects of {@link #perform}
     * must be durable.
     * If this method throws an {@link Exception}, then this transaction gets
     * aborted <em>with</em> calling {@link #rollback}, so it may leave some
     * visible side effects.
     * If this method throws an {@link Exception} and this transaction is part
     * of a composite transaction, then the preceding transactions get
     * properly rolled back.
     */
    public void commit() throws Exception { }

    /**
     * Reverts any visible side effects of the body of this transaction.
     * If this method succeeds, it must revert any visible side effects of
     * {@link #perform}.
     * If this method throws a {@link RuntimeException}, then the state of this
     * transaction is undefined and may be inconsistent.
     * If this method throws a {@link RuntimeException} and this transaction is
     * part of a composite transaction, then the preceding transactions get
     * neither committed nor rolled back and their state is undefined and may
     * be inconsistent.
     */
    public abstract void rollback() throws Exception;
}

/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.spec.cmd;

/**
 * An interface for revertible commands with durable side effects.
 * The use case is to compose revertible commands with durable side effects
 * into larger revertible commands with durable side effects.
 * <p>
 * For example updating an enterprise application in an application may be
 * composed of a list of commands to undeploy, update and redeploy the
 * enterprise application.
 * Each of these commands needs to be revertible so that if for example the
 * final redeployment step fails, the prior update and undeployment steps can
 * get reverted in order.
 * If each of the commands in the list has ACID properties, then this results
 * in the composed command to have ACID properties too, and thus work like a
 * transaction.
 * <p>
 * Note that commands are generally not idempotent, that is once they have been
 * successfully executed, you may not be able to successfully execute them
 * again because the preconditions are no longer met.
 * However, commands must be restartable, that is if {@link #perform()} has
 * thrown an exception and {@link #revert()} has succeeded, then clients can
 * still successfully execute the command as soon as the preconditions are met
 * again.
 *
 * @see Commands#execute
 * @see CompositeCommand
 * @author Christian Schlichtherle
 */
public interface Command {

    /**
     * Performs this command, thereby creating some durable side effects.
     * If this method throws an exception, then {@link #revert} must get called.
     */
    void perform() throws Exception;

    /**
     * Reverts any durable side effects of the {@link #perform} method.
     * If this method fails with an exception, then the state of the system may
     * be corrupted.
     */
    void revert() throws Exception;
}

/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.spec.cmd;

/**
 * A base class for commands which need a separate {@linkplain #doStart start}
 * method.
 *
 * @author Christian Schlichtherle
 */
abstract public class AbstractCommand implements Command {

    private boolean started;

    @Override public final void perform() throws Exception {
        if (started) throw new IllegalStateException("Already started.");
        doStart();
        started = true;
        doPerform();
    }

    @Override public final void revert() throws Exception {
        if (started) {
            doRevert();
            started = false;
        }
    }

    /**
     * Starts this command.
     * This method must not have any durable side effects.
     * If this method fails, neither {@link #doPerform} nor {@link #doRevert}
     * get called.
     */
    abstract protected void doStart() throws Exception;

    /**
     * Performs this command, thereby creating some durable side effects.
     * If this method throws an exception, then {@link #revert} must get called.
     */
    abstract protected void doPerform() throws Exception;

    /**
     * Reverts any durable side effects of the {@link #perform} method.
     * If this method fails with an exception, then the state of the system may
     * be corrupted.
     */
    abstract protected void doRevert() throws Exception;
}

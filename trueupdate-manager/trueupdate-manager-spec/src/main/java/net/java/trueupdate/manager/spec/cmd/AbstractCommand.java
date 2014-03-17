/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.spec.cmd;

/**
 * @author Christian Schlichtherle
 */
abstract public class AbstractCommand implements Command {

    private boolean started;

    @Override public final void perform() throws Exception {
        if (started) throw new IllegalStateException("Already started.");
        onStart();
        started = true;
        onPerform();
    }

    @Override public final void revert() throws Exception {
        if (started) {
            onRevert();
            started = false;
        }
    }

    abstract protected void onStart() throws Exception;

    abstract protected void onPerform() throws Exception;

    abstract protected void onRevert() throws Exception;
}

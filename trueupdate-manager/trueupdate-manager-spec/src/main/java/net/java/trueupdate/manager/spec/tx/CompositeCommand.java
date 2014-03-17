/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.spec.tx;

/**
 * A composite command.
 *
 * @author Christian Schlichtherle
 */
public final class CompositeCommand implements Command {

    private final Command[] commands;
    private int index;

    public CompositeCommand(final Command... commands) {
        if (0 >= commands.length) throw new IllegalArgumentException();
        this.commands = commands.clone();
    }

    @Override public void perform() throws Exception {
        if (0 != index) throw new IllegalStateException("Not idempotent.");
        while (index < commands.length) commands[index++].perform();
    }

    @Override public void revert() throws Exception {
        while (0 < index) {
            final int i = index - 1;
            commands[i].revert();
            index = i;
        }
    }
}

/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.plug.openejb;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Defines a simple API for revertable commands.
 *
 * @author Christian Schlichtherle
 */
interface Command {
    void execute() throws Exception;
    void revert() throws Exception;
}

final class RenameFileCommand implements Command {

    private final File from, to;

    RenameFileCommand(final File from, final File to) {
        assert null != from;
        this.from = from;
        assert null != to;
        this.to = to;
    }

    @Override public void execute() throws Exception {
        if (!from.renameTo(to)) throw ioException(from, to);
    }

    @Override public void revert() throws Exception {
        if (!to.renameTo(from)) throw ioException(to, from);
    }

    private IOException ioException(File from, File to) {
        return new IOException(String.format("Cannot rename file %s to file %s .", from, to));
    }
}

final class InverseCommand implements Command {

    private final Command tx;

    InverseCommand(final Command tx) {
        assert null != tx;
        this.tx = tx;
    }

    @Override public void execute() throws Exception {
        tx.revert();
    }

    @Override public void revert() throws Exception {
        tx.execute();
    }
}

final class Transaction implements Command {

    private static final Logger
            logger = Logger.getLogger(Transaction.class.getName());

    private final Command[] commands;

    Transaction(final Command... commands) {
        this.commands = commands.clone();
    }

    @Override public void execute() throws Exception {
        int i = 0;
        try {
            final int n = commands.length;
            for ( ; i < n; i++) commands[i].execute();
        } catch (final Exception ex) {
            try {
                while (0 <= --i) commands[i].revert();
            } catch (Exception ex2) {
                logger.log(Level.SEVERE,
                        "Failed to rollback transaction:", ex2);
            }
            throw ex;
        }
    }

    @Override public void revert() throws Exception {
        throw new UnsupportedOperationException();
    }
}

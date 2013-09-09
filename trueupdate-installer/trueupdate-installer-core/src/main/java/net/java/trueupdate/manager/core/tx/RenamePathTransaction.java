/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.core.tx;

import java.io.File;

import static net.java.trueupdate.manager.core.io.Files.renamePath;
import static net.java.trueupdate.util.Objects.requireNonNull;

/**
 * A rename path transaction.
 * The source path may refer to a file or directory.
 * An existing destination file or directory is <em>not</em> overwritten.
 *
 * @author Christian Schlichtherle
 */
public final class RenamePathTransaction extends Transaction {

    private final File from, to;

    public RenamePathTransaction(final File from, final File to) {
        this.from = requireNonNull(from);
        this.to = requireNonNull(to);
    }

    @Override public void perform() throws Exception {
        renamePath(from, to);
    }

    @Override public void rollback() throws Exception {
        if (to.exists()) renamePath(to, from);
    }
}

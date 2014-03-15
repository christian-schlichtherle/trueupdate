/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.installer.core.tx;

import java.io.File;
import java.io.IOException;
import static net.java.trueupdate.installer.core.io.Files.renamePath;
import net.java.trueupdate.manager.spec.tx.Transaction;
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

    @Override public void perform() throws IOException {
        renamePath(from, to);
    }

    @Override public void rollback() throws IOException {
        if (to.exists()) renamePath(to, from);
    }
}

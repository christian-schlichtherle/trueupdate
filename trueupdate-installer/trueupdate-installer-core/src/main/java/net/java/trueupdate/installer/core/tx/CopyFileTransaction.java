/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.installer.core.tx;

import java.io.File;
import java.io.IOException;
import static net.java.trueupdate.installer.core.io.Files.copyFile;
import static net.java.trueupdate.installer.core.io.Files.deletePath;
import net.java.trueupdate.manager.spec.tx.Transaction;
import static net.java.trueupdate.util.Objects.requireNonNull;

/**
 * A copy file transaction.
 * An existing destination file or directory is <em>not</em> overwritten.
 *
 * @author Christian Schlichtherle
 */
public final class CopyFileTransaction extends Transaction {

    private final File from, to;

    public CopyFileTransaction(final File from, final File to) {
        this.from = requireNonNull(from);
        this.to = requireNonNull(to);
    }

    @Override public void prepare() throws IOException {
        if (to.exists())
            throw new IOException(String.format(
                    "Will not overwrite existing file or directory %s .",
                    to));
    }

    @Override public void perform() throws IOException {
        copyFile(from, to);
    }

    @Override public void rollback() throws IOException {
        deletePath(to);
    }
}

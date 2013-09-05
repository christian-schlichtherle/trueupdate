/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.core.tx;

import java.io.File;
import java.io.IOException;
import static net.java.trueupdate.manager.core.io.Files.*;
import static net.java.trueupdate.shed.Objects.requireNonNull;

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

    @Override protected void prepare() throws Exception {
        if (to.exists())
            throw new IOException(String.format(
                    "Will not overwrite existing file or directory %s .",
                    to));
    }

    @Override protected void perform() throws IOException {
        copyFile(from, to);
    }

    @Override protected void rollback() throws IOException {
        deletePath(to);
    }
}
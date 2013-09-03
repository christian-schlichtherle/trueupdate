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
 * A transaction which zips a file or directory to a ZIP file.
 * An existing destination file or directory is <em>not</em> overwritten.
 *
 * @author Christian Schlichtherle
 */
public final class PatchZipTransaction extends Transaction {

    private final File inputArchive, patchArchive, outputArchive;
    private final boolean createJar;

    public PatchZipTransaction(
            final File inputArchive,
            final File patchArchive,
            final File outputArchive,
            final boolean createJar) {
        this.inputArchive = requireNonNull(inputArchive);
        this.patchArchive = requireNonNull(patchArchive);
        this.outputArchive = requireNonNull(outputArchive);
        this.createJar = createJar;
    }

    @Override protected void prepare() throws Exception {
        if (outputArchive.exists())
            throw new IOException(String.format(
                    "Will not overwrite existing file or directory %s .",
                    outputArchive));
    }

    @Override protected void perform() throws Exception {
        patchZip(inputArchive, patchArchive, outputArchive, createJar);
    }

    @Override protected void rollback() throws IOException {
        deletePath(outputArchive);
    }
}

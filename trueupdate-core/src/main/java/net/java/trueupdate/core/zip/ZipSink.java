/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.core.zip;

import edu.umd.cs.findbugs.annotations.CreatesObligation;
import java.io.IOException;

/**
 * An abstraction for writing ZIP files.
 *
 * @see    ZipSource
 * @author Christian Schlichtherle
 */
public interface ZipSink {

    /** Returns a new ZIP output for writing its entries. */
    @CreatesObligation ZipOutput output() throws IOException;
}

/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.core.io;

import edu.umd.cs.findbugs.annotations.CreatesObligation;
import java.io.*;
import java.util.zip.ZipFile;

/**
 * An abstraction for reading ZIP files.
 *
 * @see    ZipSink
 * @author Christian Schlichtherle
 */
public interface ZipSource {

    /** Returns a new ZIP file for reading its entries. */
    @CreatesObligation ZipFile input() throws IOException;
}

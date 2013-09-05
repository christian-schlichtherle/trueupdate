/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.core.zip;

import java.io.*;
import java.util.zip.*;

/**
 * The {@link ZipOutputStream} interface as it should have been.
 *
 * @see ZipInput
 * @author Christian Schlichtherle
 */
public interface ZipOutput extends Closeable {

    ZipEntry entry(String name);

    OutputStream stream(ZipEntry entry) throws IOException;
}

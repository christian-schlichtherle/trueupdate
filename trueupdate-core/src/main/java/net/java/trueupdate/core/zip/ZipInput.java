/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.core.zip;

import java.io.*;
import java.util.zip.*;
import javax.annotation.Nullable;

/**
 * The {@link ZipFile} interface as it should have been.
 *
 * @see ZipOutput
 * @author Christian Schlichtherle
 */
public interface ZipInput extends Iterable<ZipEntry>, Closeable {

    @Nullable ZipEntry entry(String name);

    InputStream stream(ZipEntry entry) throws IOException;
}

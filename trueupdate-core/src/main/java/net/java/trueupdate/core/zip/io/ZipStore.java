/*
 * Copyright (C) 2005-2013 Schlichtherle IT Services.
 * Copyright (C) 2013 Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.core.zip.io;

import java.io.IOException;

/**
 * An abstraction for reading, writing and deleting ZIP data.
 *
 * @author Christian Schlichtherle
 */
public interface ZipStore extends ZipSource, ZipSink {

    /** Deletes the ZIP data. */
    void delete() throws IOException;

    /** Returns {@code true} if and only if the ZIP data exists. */
    boolean exists();
}

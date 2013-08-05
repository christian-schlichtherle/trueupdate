/*
 * Copyright (C) 2005-2013 Schlichtherle IT Services.
 * Copyright (C) 2013 Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.core.io;

import java.io.*;
import javax.annotation.concurrent.Immutable;

/**
 * Provides a poor man's copy algorithm.
 * The implementation in this class is suitable for only small amounts of data,
 * say a few kilobytes.
 *
 * @author Christian Schlichtherle (copied and edited from TrueLicense Core 2.3.1)
 */
@Immutable
public final class Copy {

    private Copy() { }

    /*
     * Copies the data from the given source to the given sink.
     *
     * @param source the input source.
     * @param sink the output sink.
     */
    public static void copy(final Source source, final Sink sink)
    throws IOException {
        final InputStream in = source.input();
        try {
            final OutputStream out = sink.output();
            try {
                final byte[] buffer = new byte[Store.BUFSIZE];
                int read;
                while (0 <= (read = in.read(buffer)))
                    out.write(buffer, 0, read);
            } finally {
                out.close();
            }
        } finally {
            in.close();
        }
    }
}

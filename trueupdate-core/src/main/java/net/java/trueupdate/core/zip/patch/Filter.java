/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.core.zip.patch;

import net.java.trueupdate.core.zip.util.EntrySource;

/**
 * A filter for {@linkplain EntrySource entry sources}.
 *
 * @author Christian Schlichtherle
 */
interface Filter {
    /**
     * Returns {@code true} if and only if the filter accepts the given entry
     * source.
     */
    boolean accept(EntrySource entrySource);
}

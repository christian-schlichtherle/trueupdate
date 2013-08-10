/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.core.zip.patch;

import net.java.trueupdate.core.zip.util.EntrySource;

/**
 * Inverts another filter.
 *
 * @author Christian Schlichtherle
 */
final class InverseFilter implements Filter {

    private final Filter filter;

    InverseFilter(final Filter filter) {
        assert null != filter;
        this.filter = filter;
    }

    @Override public boolean accept(EntrySource entrySource) {
        return !filter.accept(entrySource);
    }
}

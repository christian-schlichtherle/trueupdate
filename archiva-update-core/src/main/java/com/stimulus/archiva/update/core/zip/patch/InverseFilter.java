/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.core.zip.patch;

import com.stimulus.archiva.update.core.zip.EntrySource;

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

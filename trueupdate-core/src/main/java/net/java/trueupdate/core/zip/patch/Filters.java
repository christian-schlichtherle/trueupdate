/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.core.zip.patch;

import net.java.trueupdate.core.util.EntrySource;

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

/**
 * A filter which accepts all {@linkplain EntrySource entry sources}.
 *
 * @author Christian Schlichtherle
 */
final class AcceptAllFilter implements Filter {

    @Override public boolean accept(EntrySource entrySource) { return true; }
}

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

/**
 * Accepts only entry sources with the name "META-INF/" or
 * "META-INF/MANIFEST.MF".
 *
 * @author Christian Schlichtherle
 */
final class ManifestFilter implements Filter {

    @Override public boolean accept(EntrySource entrySource) {
        final String name = entrySource.name();
        return "META-INF/".equals(name) || "META-INF/MANIFEST.MF".equals(name);
    }
}

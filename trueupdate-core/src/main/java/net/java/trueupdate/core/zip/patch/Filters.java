/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.core.zip.patch;

/**
 * A filter for ZIP entry names.
 *
 * @author Christian Schlichtherle
 */
interface ZipEntryNameFilter {

    /**
     * Returns {@code true} if and only if the filter accepts the given ZIP
     * entry name.
     */
    boolean accept(String name);
}


/**
 * A filter which accepts all ZIP entry names.
 *
 * @author Christian Schlichtherle
 */
final class AcceptAllZipEntryNameFilter implements ZipEntryNameFilter {

    @Override public boolean accept(String name) { return true; }
}

/**
 * Inverts another filter.
 *
 * @author Christian Schlichtherle
 */
final class InverseZipEntryNameFilter implements ZipEntryNameFilter {

    private final ZipEntryNameFilter filter;

    InverseZipEntryNameFilter(final ZipEntryNameFilter filter) {
        assert null != filter;
        this.filter = filter;
    }

    @Override public boolean accept(String name) {
        return !filter.accept(name);
    }
}
/**
 * Accepts only entry sources with the name "META-INF/" or
 * "META-INF/MANIFEST.MF".
 *
 * @author Christian Schlichtherle
 */
final class ManifestZipEntryNameFilter implements ZipEntryNameFilter {

    @Override public boolean accept(String name) {
        return "META-INF/".equals(name) || "META-INF/MANIFEST.MF".equals(name);
    }
}

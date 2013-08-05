/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.core.zip.patch;

import net.java.trueupdate.core.zip.EntrySource;

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

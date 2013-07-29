/*
 * Copyright (C) 2005-2013 Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.server.jar.patch;

import com.stimulus.archiva.update.server.jar.commons.EntrySource;

/**
 * Accepts only entries with the name "META-INF/" or "META-INF/MANIFEST.MF".
 *
 * @author Christian Schlichtherle
 */
final class ManifestFilter implements Filter {
    @Override public boolean accept(EntrySource entrySource) {
        final String name = entrySource.name();
        return "META-INF/".equals(name) || "META-INF/MANIFEST.MF".equals(name);
    }
}

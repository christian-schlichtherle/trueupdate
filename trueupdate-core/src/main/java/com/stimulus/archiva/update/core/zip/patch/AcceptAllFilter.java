/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.core.zip.patch;

import com.stimulus.archiva.update.core.zip.EntrySource;

/**
 * A filter which accepts all {@linkplain EntrySource entry sources}.
 *
 * @author Christian Schlichtherle
 */
final class AcceptAllFilter implements Filter {
    @Override public boolean accept(EntrySource entrySource) { return true; }
}

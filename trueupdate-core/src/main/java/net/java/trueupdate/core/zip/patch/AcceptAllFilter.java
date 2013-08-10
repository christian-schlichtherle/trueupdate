/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.core.zip.patch;

import net.java.trueupdate.core.zip.util.EntrySource;

/**
 * A filter which accepts all {@linkplain EntrySource entry sources}.
 *
 * @author Christian Schlichtherle
 */
final class AcceptAllFilter implements Filter {
    @Override public boolean accept(EntrySource entrySource) { return true; }
}

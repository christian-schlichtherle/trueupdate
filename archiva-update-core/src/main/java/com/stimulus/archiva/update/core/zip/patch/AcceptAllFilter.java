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

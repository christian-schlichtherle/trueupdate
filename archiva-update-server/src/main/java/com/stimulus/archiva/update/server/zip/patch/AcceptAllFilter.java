package com.stimulus.archiva.update.server.zip.patch;

import com.stimulus.archiva.update.server.zip.commons.EntrySource;

/**
 * A filter which accepts all {@linkplain EntrySource entry sources}.
 *
 * @author Christian Schlichtherle
 */
final class AcceptAllFilter implements Filter {
    @Override public boolean accept(EntrySource entrySource) { return true; }
}

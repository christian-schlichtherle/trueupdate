/*
 * Copyright (C) 2005-2013 Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.server.jar.patch;

import com.stimulus.archiva.update.server.jar.commons.EntrySource;

/**
 * A filter for entry sources.
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
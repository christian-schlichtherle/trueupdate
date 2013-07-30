/*
 * Copyright (C) 2005-2013 Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.server.jar.patch;

import java.io.IOException;

/**
 * Indicates that a ZIP entry is missing.
 *
 * @author Christian Schlichtherle
 */
public final class MissingEntryException extends IOException {

    private static final long serialVersionUID = 0L;

    MissingEntryException(String message) { super(message); }
}

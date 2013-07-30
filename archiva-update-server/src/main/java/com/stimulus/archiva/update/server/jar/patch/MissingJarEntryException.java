/*
 * Copyright (C) 2005-2013 Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.server.jar.patch;

import java.io.IOException;

/**
 * Indicates that a JAR entry is missing.
 *
 * @author Christian Schlichtherle
 */
public final class MissingJarEntryException extends IOException {

    private static final long serialVersionUID = 0L;

    MissingJarEntryException(String message) { super(message); }
}

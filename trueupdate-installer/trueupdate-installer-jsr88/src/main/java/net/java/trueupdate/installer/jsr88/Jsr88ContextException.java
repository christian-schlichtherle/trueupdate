/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.installer.jsr88;

/**
 * Indicates an error when using a {@link Jsr88Context}.
 *
 * @author Christian Schlichtherle
 */
public class Jsr88ContextException extends Exception {

    private static final long serialVersionUID = 0L;

    Jsr88ContextException(String message) {
        this(message, null);
    }

    Jsr88ContextException(String message, Throwable cause) {
        super(message, cause);
    }
}

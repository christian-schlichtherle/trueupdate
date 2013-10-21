/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.installer.jsr88;

/**
 * Indicates an error when using the JSR 88 API.
 *
 * @author Christian Schlichtherle
 */
public class Jsr88Exception extends Exception {

    private static final long serialVersionUID = 0L;

    Jsr88Exception(String message) { super(message); }

    Jsr88Exception(String message, Throwable cause) { super(message, cause); }
}

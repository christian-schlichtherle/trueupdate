/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.installer.cargo;

/**
 * Indicates an error when using a {@link CargoContext}.
 *
 * @author Christian Schlichtherle
 */
public class CargoContextException extends Exception {

    private static final long serialVersionUID = 0L;

    CargoContextException(String message, Throwable cause) {
        super(message, cause);
    }
}

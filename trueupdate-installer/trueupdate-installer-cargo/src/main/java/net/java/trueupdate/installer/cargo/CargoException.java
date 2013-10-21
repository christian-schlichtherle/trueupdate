/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.installer.cargo;

/**
 * Indicates an error when using the generic Cargo API.
 *
 * @author Christian Schlichtherle
 */
public class CargoException extends Exception {

    private static final long serialVersionUID = 0L;

    CargoException(String message, Throwable cause) {
        super(message, cause);
    }
}

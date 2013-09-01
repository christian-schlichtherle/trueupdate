/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.plug.cargo;

import java.net.URI;

/**
 * Indicates an invalid configuration URI for a {@link CargoContext}.
 *
 * @author Christian Schlichtherle
 */
public class CargoContextException extends CargoException {

    private static final long serialVersionUID = 0L;

    CargoContextException(
            final URI configuration,
            final String componentName,
            final Throwable cause) {
        super(String.format(
                "The configuration URI %s does not define a valid %s.",
                configuration, componentName), cause);
    }
}

/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.spec;

import java.io.File;

import net.java.trueupdate.manager.spec.cmd.Command;

/**
 * An update context is provided by an {@link UpdateManager} to an
 * {@link UpdateInstaller} while {@linkplain UpdateInstaller#install installing}
 * an update.
 * <p>
 * Implementations must be thread-safe.
 *
 * @author Christian Schlichtherle
 */
public interface UpdateContext {

    /** Returns the current location of the client application. */
    String currentLocation();

    /** Returns the update location of the client application. */
    String updateLocation();

    /**
     * Returns the delta ZIP file for patching the client application's
     * artifact file.
     * The caller must not modify or delete this file.
     */
    File deltaZip();

    /**
     * Decorates the given command with another command which depends on the
     * given action identifier.
     */
    Command decorate(Command cmd, ActionId id);
}

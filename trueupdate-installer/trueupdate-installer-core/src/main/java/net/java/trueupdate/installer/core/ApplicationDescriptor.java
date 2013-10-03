/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.installer.core;

import net.java.trueupdate.manager.spec.tx.Transaction;

import java.io.File;

/**
 * An application descriptor is provided by subclasses of
 * {@link CoreUpdateInstaller} while
 * {@linkplain CoreUpdateInstaller#install installing} an update.
 * <p>
 * Implementations must be thread-safe.
 *
 * @author Christian Schlichtherle
 */
public interface ApplicationDescriptor {

    /** Returns the current path of the application. */
    File currentPath();

    /** Returns the update path of the application. */
    File updatePath();

    /**
     * Returns the transaction for undeploying the application at the
     * current path.
     */
    Transaction undeploymentTransaction();

    /**
     * Returns the transaction for deploying the application at the
     * update path.
     */
    Transaction deploymentTransaction();
}

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

    /**
     * Returns the transaction for the undeployment of the current application
     * at the {@link #currentPath}.
     * The transaction gets decorated with logging statements and composed
     * into a {@link net.java.trueupdate.manager.spec.tx.CompositeTransaction}
     * before execution.
     */
    Transaction undeploymentTransaction();

    /** Returns the update path of the application. */
    File updatePath();

    /**
     * Returns the transaction for the deployment of the updated application
     * at the {@link #updatePath}.
     * The transaction gets decorated with logging statements and composed
     * into a {@link net.java.trueupdate.manager.spec.tx.CompositeTransaction}
     * before execution.
     */
    Transaction deploymentTransaction();
}

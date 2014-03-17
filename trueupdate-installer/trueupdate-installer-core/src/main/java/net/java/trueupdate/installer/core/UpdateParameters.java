/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.installer.core;

import net.java.trueupdate.manager.spec.cmd.Command;

import java.io.File;

/**
 * Update parameters are provided by subclasses of {@link CoreUpdateInstaller}
 * while {@linkplain CoreUpdateInstaller#install installing} an update.
 * <p>
 * Implementations must be thread-safe.
 *
 * @author Christian Schlichtherle
 */
public interface UpdateParameters {

    /** Returns the current path of the application. */
    File currentPath();

    /**
     * Returns the transaction command for the undeployment of the current
     * application at the {@link #currentPath}.
     * The command gets decorated with logging statements and composed
     * into a {@link net.java.trueupdate.manager.spec.cmd.CompositeCommand}
     * before execution.
     */
    Command undeploymentTransaction();

    /** Returns the update path of the application. */
    File updatePath();

    /**
     * Returns the transaction command for the deployment of the updated
     * application at the {@link #updatePath}.
     * The command gets decorated with logging statements and composed
     * into a {@link net.java.trueupdate.manager.spec.cmd.CompositeCommand}
     * before execution.
     */
    Command deploymentTransaction();
}

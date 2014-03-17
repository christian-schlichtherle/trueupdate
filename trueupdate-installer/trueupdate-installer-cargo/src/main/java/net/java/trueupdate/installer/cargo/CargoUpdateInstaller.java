/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.installer.cargo;

import java.io.File;
import java.net.URI;
import javax.annotation.concurrent.Immutable;
import net.java.trueupdate.installer.core.CoreUpdateInstaller;
import net.java.trueupdate.installer.core.UpdateParameters;
import net.java.trueupdate.manager.spec.UpdateContext;
import net.java.trueupdate.manager.spec.cmd.Command;

/**
 * Installs updates for applications running in a container which is supported
 * by Cargo.
 *
 * @author Christian Schlichtherle
 */
@Immutable
public final class CargoUpdateInstaller extends CoreUpdateInstaller {

    @Override
    protected UpdateParameters updateParameters(final UpdateContext uc)
    throws Exception {

        final CargoContext cctx = new CargoContext(new URI(uc.currentLocation()));
        final File cpath = cctx.deployablePath();

        final CargoContext uctx = new CargoContext(new URI(uc.updateLocation()));
        final File upath = uctx.deployablePath();

        class ResolvedParameters implements UpdateParameters {

            @Override public File currentPath() { return cpath; }

            @Override public Command undeploymentTransaction() {
                return cctx.undeploymentTransaction();
            }

            @Override public File updatePath() { return upath; }

            @Override public Command deploymentTransaction() {
                return uctx.deploymentTransaction();
            }
        } // ResolvedParameters

        return new ResolvedParameters();
    }
}

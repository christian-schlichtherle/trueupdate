/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.installer.jsr88;

import java.io.File;
import java.net.URI;
import javax.annotation.concurrent.Immutable;
import javax.enterprise.deploy.spi.factories.DeploymentFactory;
import net.java.trueupdate.installer.core.CoreUpdateInstaller;
import net.java.trueupdate.installer.core.UpdateParameters;
import net.java.trueupdate.manager.spec.UpdateContext;
import net.java.trueupdate.manager.spec.cmd.Command;
import net.java.trueupdate.util.Services;

/**
 * Installs updates for applications running in a container which supports
 * JSR-88.
 *
 * @author Christian Schlichtherle
 */
@Immutable
public final class Jsr88UpdateInstaller extends CoreUpdateInstaller {

    @Override
    protected UpdateParameters updateParameters(final UpdateContext uc)
    throws Exception {

        final DeploymentFactory df = Services.load(DeploymentFactory.class);
        final Jsr88Context cctx = new Jsr88Context(new URI(uc.currentLocation()), df);
        final Jsr88Context uctx = new Jsr88Context(new URI(uc.updateLocation()), df);

        class ResolvedParameters implements UpdateParameters {

            @Override public File currentPath() { return cctx.moduleArchive(); }

            @Override public Command undeploymentTransaction() {
                return cctx.undeploymentTransaction();
            }

            @Override public File updatePath() { return uctx.moduleArchive(); }

            @Override public Command deploymentTransaction() {
                return uctx.deploymentTransaction();
            }
        } // ResolvedParameters

        return new ResolvedParameters();
    }
}

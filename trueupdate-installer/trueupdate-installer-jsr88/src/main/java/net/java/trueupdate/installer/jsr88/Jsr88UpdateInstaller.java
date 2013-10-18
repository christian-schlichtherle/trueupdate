/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.installer.jsr88;

import java.io.File;
import java.net.URI;
import javax.annotation.concurrent.Immutable;
import javax.enterprise.deploy.spi.factories.DeploymentFactory;
import net.java.trueupdate.installer.core.*;
import net.java.trueupdate.manager.spec.UpdateContext;
import net.java.trueupdate.manager.spec.tx.Transaction;
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
        final Jsr88Context cjc = new Jsr88Context(df, new URI(uc.currentLocation()));
        final Jsr88Context ujc = new Jsr88Context(df, new URI(uc.updateLocation()));

        class ResolvedParameters implements UpdateParameters {

            @Override public File currentPath() { return cjc.moduleArchive(); }

            @Override public Transaction undeploymentTransaction() {
                return cjc.undeploymentTransaction();
            }

            @Override public File updatePath() { return ujc.moduleArchive(); }

            @Override public Transaction deploymentTransaction() {
                return ujc.deploymentTransaction();
            }
        } // ResolvedParameters

        return new ResolvedParameters();
    }
}

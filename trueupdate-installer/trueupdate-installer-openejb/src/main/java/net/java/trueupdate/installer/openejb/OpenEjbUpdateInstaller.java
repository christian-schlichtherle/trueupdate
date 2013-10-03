/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.installer.openejb;

import java.io.*;
import java.net.URI;
import javax.annotation.concurrent.Immutable;
import javax.ejb.EJB;

import net.java.trueupdate.installer.core.ApplicationDescriptor;
import net.java.trueupdate.installer.core.CoreUpdateInstaller;
import net.java.trueupdate.manager.spec.UpdateContext;
import net.java.trueupdate.manager.spec.tx.AtomicMethodsTransaction;
import net.java.trueupdate.manager.spec.tx.Transaction;
import org.apache.openejb.assembler.Deployer;
import org.apache.openejb.assembler.classic.AppInfo;

/**
 * Installs updates for applications running in OpenEJB.
 *
 * @author Christian Schlichtherle
 */
@Immutable
public final class OpenEjbUpdateInstaller extends CoreUpdateInstaller {

    private @EJB Deployer deployer;

    @Override
    protected ApplicationDescriptor applicationDescriptor(
            final UpdateContext uc)
    throws Exception {

        final File cpath = resolveCurrentPath(new URI(uc.currentLocation()));
        final File upath = uc.updateLocation().equals(uc.currentLocation())
                ? cpath : new File(uc.updateLocation());

        class ResolvedDescriptor implements ApplicationDescriptor {

            @Override public File currentPath() { return cpath; }

            @Override public File updatePath() { return upath; }

            @Override public Transaction undeploymentTransaction() {

                class UndeploymentTransaction extends AtomicMethodsTransaction {

                    @Override public void performAtomic() throws Exception {
                        deployer.undeploy(cpath.getPath());
                    }

                    @Override public void rollbackAtomic() throws Exception {
                        deployer.deploy(cpath.getPath());
                    }
                } // UndeploymentTransaction

                return new UndeploymentTransaction();
            }

            @Override public Transaction deploymentTransaction() {

                class DeploymentTransaction extends AtomicMethodsTransaction {

                    @Override public void performAtomic() throws Exception {
                        deployer.deploy(upath.getPath());
                    }

                    @Override public void rollbackAtomic() throws Exception {
                        deployer.undeploy(upath.getPath());
                    }
                } // DeploymentTransaction

                return new DeploymentTransaction();
            }
        } // ResolvedDescriptor

        return new ResolvedDescriptor();
    }

    private File resolveCurrentPath(final URI location) throws Exception {
        final Scheme scheme = Scheme.valueOf(location.getScheme());
        for (final AppInfo info : deployer.getDeployedApps())
            if (scheme.matches(location, info))
                return new File(info.path);
        throw new FileNotFoundException(String.format(
                "Cannot locate application at %s .", location));
    }

    private enum Scheme {
        app {
            @Override boolean matches(URI location, AppInfo info) {
                return location.getSchemeSpecificPart().equals(info.appId);
            }
        },

        file {
            @Override boolean matches(URI location, AppInfo info) {
                return new File(location).equals(new File(info.path));
            }
        };

        abstract boolean matches(URI location, AppInfo info);
    }
}

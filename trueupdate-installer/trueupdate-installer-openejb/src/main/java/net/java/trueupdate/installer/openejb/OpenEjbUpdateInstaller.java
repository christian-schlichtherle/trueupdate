/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.installer.openejb;

import java.io.*;
import java.net.URI;
import javax.annotation.concurrent.Immutable;
import javax.ejb.EJB;
import net.java.trueupdate.installer.core.LocalUpdateInstaller;
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
public final class OpenEjbUpdateInstaller extends LocalUpdateInstaller {

    private @EJB Deployer deployer;

    @Override
    protected LocationContext locationContext(final UpdateContext context,
                                              final String location)
    throws Exception {

        final File path = location.equals(context.currentLocation())
                ? resolveDeployedPath(new URI(location))
                : new File(location);

        class ResolvedLocationContext implements LocationContext {

            @Override public File path() { return path; }

            @Override public Transaction undeploymentTransaction() {

                class UndeploymentTransaction extends AtomicMethodsTransaction {

                    @Override public void performAtomic() throws Exception {
                        deployer.undeploy(path.getPath());
                    }

                    @Override public void rollbackAtomic() throws Exception {
                        deployer.deploy(path.getPath());
                    }
                } // UndeploymentTransaction

                return new UndeploymentTransaction();
            }

            @Override public Transaction deploymentTransaction() {

                class DeploymentTransaction extends AtomicMethodsTransaction {

                    @Override public void performAtomic() throws Exception {
                        deployer.deploy(path.getPath());
                    }

                    @Override public void rollbackAtomic() throws Exception {
                        deployer.undeploy(path.getPath());
                    }
                } // DeploymentTransaction

                return new DeploymentTransaction();
            }
        } // ResolvedLocationContext

        return new ResolvedLocationContext();
    }

    private File resolveDeployedPath(final URI location) throws Exception {
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

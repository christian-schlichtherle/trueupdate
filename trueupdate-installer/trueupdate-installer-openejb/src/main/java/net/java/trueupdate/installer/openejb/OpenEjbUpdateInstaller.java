/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.installer.openejb;

import net.java.trueupdate.installer.core.CoreUpdateInstaller;
import net.java.trueupdate.installer.core.UpdateParameters;
import net.java.trueupdate.manager.spec.UpdateContext;
import net.java.trueupdate.manager.spec.cmd.Command;
import net.java.trueupdate.manager.spec.cmd.Commands;
import org.apache.openejb.assembler.Deployer;
import org.apache.openejb.assembler.classic.AppInfo;

import javax.annotation.concurrent.Immutable;
import javax.ejb.EJB;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;

/**
 * Installs updates for applications running in Apache OpenEJB.
 *
 * @author Christian Schlichtherle
 */
@Immutable
public final class OpenEjbUpdateInstaller extends CoreUpdateInstaller {

    private @EJB Deployer deployer;

    @Override
    protected UpdateParameters updateParameters(final UpdateContext uc)
    throws Exception {

        final File cpath = resolveCurrentPath(new URI(uc.currentLocation()));
        final File upath = uc.updateLocation().equals(uc.currentLocation())
                ? cpath : new File(uc.updateLocation());

        class ResolvedParameters implements UpdateParameters {

            @Override public File currentPath() { return cpath; }

            @Override public Command undeploymentTransaction() {

                class UndeploymentCommand implements Command {

                    @Override public void perform() throws Exception {
                        deployer.undeploy(cpath.getPath());
                    }

                    @Override public void revert() throws Exception {
                        deployer.deploy(cpath.getPath());
                    }
                } // UndeploymentCommand

                return Commands.atomic(new UndeploymentCommand());
            }

            @Override public File updatePath() { return upath; }

            @Override public Command deploymentTransaction() {

                class DeploymentCommand implements Command {

                    @Override public void perform() throws Exception {
                        deployer.deploy(upath.getPath());
                    }

                    @Override public void revert() throws Exception {
                        deployer.undeploy(upath.getPath());
                    }
                } // DeploymentCommand

                return Commands.atomic(new DeploymentCommand());
            }
        } // ResolvedParameters

        return new ResolvedParameters();
    }

    private File resolveCurrentPath(final URI location) throws Exception {
        final Scheme scheme = Scheme.valueOf(location.getScheme());
        for (final AppInfo info : deployer.getDeployedApps())
            if (scheme.matches(location, info))
                return new File(info.path);
        throw new FileNotFoundException(String.format(
                "Could not locate application at %s .", location));
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

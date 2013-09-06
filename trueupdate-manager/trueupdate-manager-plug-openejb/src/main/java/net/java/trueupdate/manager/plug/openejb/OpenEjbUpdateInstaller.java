/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.plug.openejb;

import java.io.*;
import java.net.URI;
import java.util.*;
import javax.annotation.concurrent.Immutable;
import javax.ejb.EJB;
import net.java.trueupdate.manager.core.*;
import net.java.trueupdate.manager.core.tx.Transaction;
import net.java.trueupdate.manager.spec.UpdateMessage;
import static net.java.trueupdate.shed.Objects.requireNonNull;
import org.apache.openejb.assembler.Deployer;
import org.apache.openejb.assembler.classic.AppInfo;

/**
 * Installs updates for applications running in OpenEJB.
 *
 * @author Christian Schlichtherle
 */
@Immutable
public final class OpenEjbUpdateInstaller implements UpdateInstaller {

    private @EJB Deployer deployer;

    @Override public void install(final UpdateResolver resolver,
                                  final UpdateMessage message)
    throws Exception {

        final Map<URI, String> paths = paths(message);

        abstract class DeployerTx extends Transaction {

            boolean performed;

            @Override protected void prepare() throws Exception {
                if (performed) throw new IllegalStateException();
            }

            @Override protected void commit() throws Exception {
                performed = false;
            }
        } // DeployerTx

        class ConfiguredUpdateInstaller extends LocalUpdateInstaller {

            @Override protected File resolvePath(URI location) throws Exception {
                return new File(paths.get(location));
            }

            @Override protected Transaction deploymentTx(final URI location) {

                class DeploymentTx extends DeployerTx {

                    final String path = requireNonNull(paths.get(location));

                    @Override public void perform() throws Exception {
                        deployer.deploy(path);
                        performed = true;
                    }

                    @Override public void rollback() throws Exception {
                        if (performed) {
                            deployer.undeploy(path);
                            performed = false;
                        }
                    }
                } // DeploymentTx

                return new DeploymentTx();
            }

            @Override protected Transaction undeploymentTx(final URI location) {

                class UndeploymentTx extends DeployerTx {

                    final String path = requireNonNull(paths.get(location));

                    @Override public void perform() throws Exception {
                        deployer.undeploy(path);
                        performed = true;
                    }

                    @Override public void rollback() throws Exception {
                        if (performed) {
                            deployer.deploy(path);
                            performed = false;
                        }
                    }
                } // UndeploymentTx

                return new UndeploymentTx();
            }
        } // ConfiguredUpdateInstaller

        new ConfiguredUpdateInstaller().install(resolver, message);
    }

    Map<URI, String> paths(UpdateMessage message) throws Exception {
        final URI currentLocation = message.currentLocation();
        final String currentPath = path(currentLocation);
        final URI updateLocation = message.updateLocation();
        final Map<URI, String> paths = new HashMap<URI, String>();
        paths.put(currentLocation, currentPath);
        paths.put(updateLocation, currentLocation.equals(updateLocation)
                ? currentPath : new File(updateLocation).getPath());
        return paths;
    }

    private String path(final URI location) throws Exception {
        final Scheme scheme = Scheme.valueOf(location.getScheme());
        for (final AppInfo info : deployer.getDeployedApps())
            if (scheme.matches(location, info))
                return info.path;
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

/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.plug.openejb;

import java.io.*;
import java.net.URI;
import javax.annotation.concurrent.Immutable;
import javax.ejb.EJB;
import net.java.trueupdate.manager.core.*;
import net.java.trueupdate.manager.core.tx.Transaction;
import net.java.trueupdate.manager.spec.UpdateMessage;
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

        class ConfiguredUpdateInstaller extends LocalUpdateInstaller {

            @Override protected Context resolveContext(final URI location)
            throws Exception {

                final File path = location.equals(message.currentLocation())
                        ? resolveDeployedPath(location)
                        : new File(location);

                class ResolvedContext implements Context {

                    @Override public File path() { return path; }

                    @Override public Transaction deploymentTx() {

                        class DeploymentTx extends DeployerTx {

                            @Override public void perform() throws Exception {
                                deployer.deploy(path.getPath());
                                performed = true;
                            }

                            @Override public void rollback() throws Exception {
                                if (performed) {
                                    deployer.undeploy(path.getPath());
                                    performed = false;
                                }
                            }
                        } // DeploymentTx

                        return new DeploymentTx();
                    }

                    @Override public Transaction undeploymentTx() {

                        class UndeploymentTx extends DeployerTx {

                            @Override public void perform() throws Exception {
                                deployer.undeploy(path.getPath());
                                performed = true;
                            }

                            @Override public void rollback() throws Exception {
                                if (performed) {
                                    deployer.deploy(path.getPath());
                                    performed = false;
                                }
                            }
                        } // UndeploymentTx

                        return new UndeploymentTx();
                    }
                } // ResolvedContext

                return new ResolvedContext();
            }

            private File resolveDeployedPath(final URI location) throws Exception {
                final Scheme scheme = Scheme.valueOf(location.getScheme());
                for (final AppInfo info : deployer.getDeployedApps())
                    if (scheme.matches(location, info))
                        return new File(info.path);
                throw new FileNotFoundException(String.format(
                        "Cannot locate application at %s .", location));
            }
        } // ConfiguredUpdateInstaller

        new ConfiguredUpdateInstaller().install(resolver, message);
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

    private static abstract class DeployerTx extends Transaction {

        boolean performed;

        @Override protected void prepare() throws Exception {
            if (performed) throw new IllegalStateException();
        }

        @Override protected void commit() throws Exception {
            performed = false;
        }
    } // DeployerTx
}

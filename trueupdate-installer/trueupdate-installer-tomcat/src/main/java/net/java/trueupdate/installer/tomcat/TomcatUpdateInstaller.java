/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.installer.tomcat;

import java.io.File;
import java.io.IOException;
import javax.annotation.concurrent.Immutable;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import net.java.trueupdate.installer.core.CoreUpdateInstaller;
import net.java.trueupdate.installer.core.UpdateParameters;
import net.java.trueupdate.installer.core.io.Files;
import net.java.trueupdate.manager.spec.UpdateContext;
import net.java.trueupdate.manager.spec.tx.AtomicMethodsTransaction;
import net.java.trueupdate.manager.spec.tx.Transaction;
import org.apache.catalina.Engine;
import org.apache.catalina.Globals;
import org.apache.catalina.Host;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.startup.HostConfig;
import org.apache.catalina.util.ContextName;

/**
 * Installs updates for applications running in Apache Tomcat.
 *
 * @author Christian Schlichtherle
 */
@Immutable
public final class TomcatUpdateInstaller extends CoreUpdateInstaller {

    private static final ObjectName pattern;

    static {
        try {
            pattern = new ObjectName("*:type=Engine");
        } catch (MalformedObjectNameException ex) {
            throw new AssertionError(ex);
        }
    }

    private Host host;
    private HostConfig config;

    public TomcatUpdateInstaller() {
        for (final MBeanServer mbs : MBeanServerFactory.findMBeanServer(null)) {
            for (final ObjectName on : mbs.queryNames(pattern, null)) {
                try {
                    final Engine engine = (Engine) mbs.getAttribute(on, "managedResource");
                    final Host host = (Host) engine.findChild(engine.getDefaultHost());
                    if (null != host) {
                        this.host = host;
                        for (final LifecycleListener listener : host.findLifecycleListeners()) {
                            if (listener instanceof HostConfig) {
                                this.config = (HostConfig) listener;
                                return;
                            }
                        }
                    }
                } catch (Exception ignored) {
                }
            }
        }
    }

    @Override
    protected UpdateParameters updateParameters(final UpdateContext uc)
    throws Exception {

        class ResolvedParameters implements UpdateParameters {

            final File appBase = appBase();

            final ContextName ccn = new ContextName(uc.currentLocation(), true);
            final String cname = ccn.getName();
            final File cdir = new File(appBase, ccn.getBaseName());
            final File cwar = new File(cdir.getPath() + ".war");

            final boolean warDeployment = cwar.isFile();

            final ContextName ucn = new ContextName(uc.updateLocation(), true);
            final String uname = ucn.getName();
            final File udir = new File(appBase, ucn.getBaseName());
            final File uwar = new File(udir.getPath() + ".war");

            @Override public File currentPath() {
                return warDeployment ? cwar : cdir;
            }

            @Override public Transaction undeploymentTransaction() {

                class UndeploymentTransaction extends AtomicMethodsTransaction {

                    @Override public void prepareAtomic() throws Exception {
                        if (!config.isDeployed(cname))
                            throw new Exception(String.format(
                                    "The application %s is not deployed.",
                                    ccn.getDisplayName()));
                        config.addServiced(cname);
                    }

                    @Override public void performAtomic() throws Exception {
                        config.unmanageApp(cname);
                        if (warDeployment) Files.deletePath(cdir);
                    }

                    @Override public void rollbackAtomic() {
                        try { config.check(cname); }
                        finally { config.removeServiced(cname); }
                    }

                    @Override public void commitAtomic() {
                        config.removeServiced(cname);
                    }
                } // UndeploymentTransaction

                return new UndeploymentTransaction();
            }

            @Override public File updatePath() {
                return warDeployment ? uwar : udir;
            }

            @Override public Transaction deploymentTransaction() {

                class DeploymentTransaction extends AtomicMethodsTransaction {

                    @Override
                    public void prepareAtomic() throws Exception {
                        assert !config.isDeployed(uname);
                    }

                    @Override public void performAtomic() throws Exception {
                        config.check(uname);
                    }

                    @Override public void rollbackAtomic() {
                        config.unmanageApp(uname);
                        if (warDeployment) try {
                            Files.deletePath(udir);
                        } catch (IOException ex) {
                            throw new IllegalStateException(ex);
                        }
                    }
                } // DeploymentTransaction

                return new DeploymentTransaction();
            }
        } // ResolvedParameters

        if (null == host || null == config)
            throw new Exception("The application is not running in Tomcat.");
        return new ResolvedParameters();
    }

    File appBase() {
        File appBase = new File(host.getAppBase());
        if (!appBase.isAbsolute()) {
            final File parent = new File(System.getProperty(
                    Globals.CATALINA_BASE_PROP));
            appBase = new File(parent, appBase.getPath());
        }
        try {
            return appBase.getCanonicalFile();
        } catch (IOException e) {
            return appBase;
        }
    }
}

/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.installer.tomcat;

import java.io.*;
import javax.annotation.concurrent.Immutable;
import javax.management.*;
import net.java.trueupdate.installer.core.LocalUpdateInstaller;
import net.java.trueupdate.installer.core.io.Files;
import net.java.trueupdate.installer.core.tx.*;
import net.java.trueupdate.message.UpdateMessage;
import org.apache.catalina.*;
import org.apache.catalina.startup.HostConfig;
import org.apache.catalina.util.ContextName;

/**
 * Installs updates for applications running in OpenEJB.
 *
 * @author Christian Schlichtherle
 */
@Immutable
public final class TomcatUpdateInstaller extends LocalUpdateInstaller {

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
                    if (null != host)
                        this.host = host;
                        for (final LifecycleListener listener : host.findLifecycleListeners())
                            if (listener instanceof HostConfig) {
                                this.config = (HostConfig) listener;
                                return;
                            }
                } catch (Exception ignored) {
                }
            }
        }
    }

    @Override
    protected Context resolveContext(final UpdateMessage message,
                                     final String location)
    throws Exception {

        class ResolvedContext implements Context {

            final ContextName cn = new ContextName(location);
            final String name = cn.getName();
            final File dir = new File(appBase(), cn.getBaseName());
            final File war = new File(dir.getPath() + ".war");
            final File path = war.isFile() ? war : dir;

            @Override public File path() { return path; }

            @Override public Transaction undeploymentTransaction() {

                class UndeploymentTransaction extends AtomicMethodsTransaction {

                    @Override public void prepareAtomic() throws Exception {
                        if (!config.isDeployed(name))
                            throw new Exception(String.format(
                                    "The application %s is not deployed.",
                                    cn.getDisplayName()));
                        config.addServiced(name);
                    }

                    @Override public void performAtomic() throws Exception {
                        config.unmanageApp(name);
                        cleanupUnwantedSideEffectsOfDeployment();
                    }

                    @Override public void rollbackAtomic() throws Exception {
                        try {
                            config.check(name);
                        } finally {
                            config.removeServiced(name);
                        }
                    }

                    @Override public void commitAtomic() throws Exception {
                        config.removeServiced(name);
                    }
                } // UndeploymentTransaction

                return new UndeploymentTransaction();
            }

            @Override public Transaction deploymentTransaction() {

                class DeploymentTransaction extends AtomicMethodsTransaction {

                    @Override
                    public void prepareAtomic() throws Exception {
                        assert !config.isDeployed(name);
                    }

                    @Override public void performAtomic() throws Exception {
                        config.check(name);
                    }

                    @Override public void rollbackAtomic() throws Exception {
                        config.unmanageApp(name);
                        cleanupUnwantedSideEffectsOfDeployment();
                    }
                } // DeploymentTransaction

                return new DeploymentTransaction();
            }

            void cleanupUnwantedSideEffectsOfDeployment() throws IOException {
                if (path == war) Files.deletePath(dir);
            }
        } // ResolvedContext

        if (null == host || null == config)
            throw new Exception("This application is not running in Tomcat.");
        return new ResolvedContext();
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

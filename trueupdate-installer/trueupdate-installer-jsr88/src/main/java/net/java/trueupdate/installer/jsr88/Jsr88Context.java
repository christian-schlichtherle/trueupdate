/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.installer.jsr88;

import java.io.*;
import java.net.URI;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.shared.StateType;
import javax.enterprise.deploy.spi.*;
import javax.enterprise.deploy.spi.exceptions.*;
import javax.enterprise.deploy.spi.factories.DeploymentFactory;
import javax.enterprise.deploy.spi.status.DeploymentStatus;
import javax.enterprise.deploy.spi.status.ProgressEvent;
import javax.enterprise.deploy.spi.status.ProgressListener;
import javax.enterprise.deploy.spi.status.ProgressObject;
import net.java.trueupdate.installer.core.util.Uris;
import net.java.trueupdate.manager.spec.tx.*;
import static net.java.trueupdate.util.Objects.nonNullOr;

/**
 * A context which decomposes a location URI to configure various parameters
 * and perform a redeployment using the JSR 88 API.
 *
 * @author Christian Schlichtherle
 */
@Immutable
final class Jsr88Context {

    private final DeploymentFactory df;
    private final URI location;
    private final Map<String, List<String>> parameters;

    Jsr88Context(final DeploymentFactory df, final URI location) {
        assert null != df;
        this.df = df;
        this.parameters = Uris.queryParameters(location);
        this.location = location;
    }

    Transaction deploymentTransaction() {
        return new DeploymentTransaction();
    }

    private void deployAndStart() throws Jsr88ContextException {
        deploy();
        start();
    }

    private void deploy() throws Jsr88ContextException {
        monitor(CommandType.DISTRIBUTE,
                deploymentManager()
                    .distribute(targets(), moduleArchive(), deploymentPlan()));
    }

    private void start() throws Jsr88ContextException {
        monitor(CommandType.START,
                deploymentManager().start(targetModuleIDs()));
    }

    Transaction undeploymentTransaction() {
        return new UndeploymentTransaction();
    }

    private void stopAndUndeploy() throws Jsr88ContextException {
        stop();
        undeploy();
    }

    private void stop() throws Jsr88ContextException {
        monitor(CommandType.STOP,
                deploymentManager().stop(targetModuleIDs()));
    }

    private void undeploy() throws Jsr88ContextException {
        monitor(CommandType.UNDEPLOY,
                deploymentManager().undeploy(targetModuleIDs()));
    }

    private void monitor(final CommandType command, final ProgressObject po)
    throws Jsr88ContextException {

        class Monitor implements ProgressListener, Callable<Void> {

            final Logger logger = Logger.getLogger(Jsr88Context.class.getName());

            @Override
            public void handleProgressEvent(final ProgressEvent event) {
                final TargetModuleID tmid = event.getTargetModuleID();
                final DeploymentStatus ds = event.getDeploymentStatus();
                final Level level = ds.isFailed()
                        ? Level.WARNING
                        : ds.isCompleted() ? Level.FINE : Level.FINER;
                // Note that ds.getCommand() returns null with
                // org.glassfish.main.deploy:deployment-client:4.0, although it
                // really shouldn't!
                logger.log(level, "{0} command {1,choice,0#is|0<has} {2} with message \"{3}\" on target {4}.",
                        new Object[] {
                            ds.getCommand(),
                            ds.getState().getValue(),
                            ds.getState(),
                            ds.getMessage(),
                            tmid.getTarget().getName()
                        });
                synchronized (this) { notifyAll(); }
            }

            @Override public Void call() throws Jsr88ContextException {
                po.addProgressListener(this);
                try {
                    while (!command.equals(command())
                            || StateType.RUNNING.equals(state())) {
                        synchronized (this) {
                            wait();
                        }
                    }
                } catch (InterruptedException ex) {
                    throw new Jsr88ContextException(String.format(
                            "Interrupted while waiting for the %s command to complete.",
                            command()), ex);
                } finally {
                    po.removeProgressListener(this);
                }
                if (!state().equals(StateType.COMPLETED))
                    throw new Jsr88ContextException(String.format("Could not complete %s command.", command()));
                return null;
            }

            // Note that this method returns null with
            // org.glassfish.main.deploy:deployment-client:4.0, although it
            // really shouldn't!
            @Nullable CommandType command() {
                return deploymentStatus().getCommand();
            }

            StateType state() { return deploymentStatus().getState(); }

            DeploymentStatus deploymentStatus() {
                return po.getDeploymentStatus();
            }
        } // Monitor

        new Monitor().call();
    }

    private TargetModuleID[] targetModuleIDs() throws Jsr88ContextException {
        final DeploymentManager dm = deploymentManager();
        final String mid = moduleID();
        final Target[] targets = targets();
        final Collection<TargetModuleID>
                tmids = new ArrayList<TargetModuleID>(targets.length);
        try {
            for (final TargetModuleID tmid : dm.getAvailableModules(moduleType(), targets))
                if (mid.equals(tmid.getModuleID()))
                    tmids.add(tmid);
        } catch (TargetException ex) {
            throw new AssertionError(ex);
        }
        if (tmids.isEmpty())
            throw new Jsr88ContextException(
                    String.format("The module %s is not deployed.", mid));
        return tmids.toArray(new TargetModuleID[tmids.size()]);
    }

    private Target[] targets() throws Jsr88ContextException {
        return deploymentManager().getTargets();
    }

    private DeploymentManager deploymentManager() throws Jsr88ContextException {
        try {
            return df.getDeploymentManager(uri(), username(), password());
        } catch (DeploymentManagerCreationException ex) {
            throw new Jsr88ContextException("Could not create deployment manager.", ex);
        }
    }

    private ModuleType moduleType() throws Jsr88ContextException {
        final String scheme = location.getScheme().toLowerCase(Locale.ENGLISH);

             if ("car".equals(scheme)) return ModuleType.CAR;
        else if ("ear".equals(scheme)) return ModuleType.EAR;
        else if ("ejb".equals(scheme)) return ModuleType.EJB;
        else if ("rar".equals(scheme)) return ModuleType.RAR;
        else if ("war".equals(scheme)) return ModuleType.WAR;

        throw new Jsr88ContextException(
                String.format("Unknown module type %s.", scheme));
    }

    File moduleArchive() { return new File(location.getPath()); }

    private String moduleID() { return parameter("moduleID"); }
    private String uri() { return parameter("uri"); }
    private String username() { return parameter("username"); }
    private String password() { return parameter("password"); }

    private @Nullable File deploymentPlan() {
        final String dp = parameter("deploymentPlan", null);
        return null == dp ? null : new File(dp);
    }

    private String parameter(String name) { return parameter(name, ""); }

    private String parameter(final String name, final String defaultValue) {
        for (String p : parameters(name)) return p;
        return defaultValue;
    }

    private List<String> parameters(String name) {
        return nonNullOr(parameters.get(name), Collections.<String>emptyList());
    }

    private abstract class RedeploymentTransaction
    extends AtomicMethodsTransaction {

        @Override public void prepareAtomic() throws Exception {
            final File ma = moduleArchive();
            if (!ma.exists())
                throw new FileNotFoundException(String.format(
                        "The module archive %s does not exist.",
                        ma,
                        moduleID()));
        }
    } // RedeploymentTransaction

    private final class DeploymentTransaction
    extends RedeploymentTransaction {

        @Override public void performAtomic() throws Exception {
            deployAndStart();
        }

        @Override public void rollbackAtomic() {
            try {
                stopAndUndeploy();
            } catch (Jsr88ContextException ex) {
                throw new IllegalStateException(ex);
            }
        }
    } // DeploymentTransaction

    private final class UndeploymentTransaction
    extends RedeploymentTransaction {

        @Override public void prepareAtomic() throws Exception {
            super.prepareAtomic();
            targetModuleIDs(); // check deployed
        }

        @Override public void performAtomic() throws Exception {
            stopAndUndeploy();
        }

        @Override public void rollbackAtomic() {
            try {
                deployAndStart();
            } catch (Jsr88ContextException ex) {
                throw new IllegalStateException(ex);
            }
        }
    } // UndeploymentTransaction
}

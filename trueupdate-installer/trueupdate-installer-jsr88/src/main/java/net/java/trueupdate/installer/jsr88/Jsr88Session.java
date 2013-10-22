/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.installer.jsr88;

import edu.umd.cs.findbugs.annotations.CleanupObligation;
import edu.umd.cs.findbugs.annotations.CreatesObligation;
import edu.umd.cs.findbugs.annotations.DischargesObligation;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.shared.StateType;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException;
import javax.enterprise.deploy.spi.exceptions.TargetException;
import javax.enterprise.deploy.spi.status.DeploymentStatus;
import javax.enterprise.deploy.spi.status.ProgressEvent;
import javax.enterprise.deploy.spi.status.ProgressListener;
import javax.enterprise.deploy.spi.status.ProgressObject;
import static net.java.trueupdate.util.Objects.nonNullOr;

/**
 * A JSR 88 session is given a JSR 88 context and creates a connected
 deployment manager.
 * The connected deployment manager should be released by calling
 {@link #close()}.
 *
 * @author Christian Schlichtherle
 */
@CleanupObligation
final class Jsr88Session implements Closeable {

    private final Jsr88Context ctx;
    private final DeploymentManager dm;

    private volatile String effectiveModuleID;

    @CreatesObligation
    Jsr88Session(final Jsr88Context ctx) throws Jsr88Exception {
        this.ctx = ctx;
        this.effectiveModuleID = ctx.moduleID();
        try {
            this.dm = ctx.deploymentFactory().getDeploymentManager(
                    ctx.uri(), ctx.username(), ctx.password());
        } catch (DeploymentManagerCreationException ex) {
            throw new Jsr88Exception("Could not create JSR 88 session.", ex);
        }
    }

    void checkAvailable() throws Jsr88Exception {
        if (0 == targetModuleIDs().length)
            throw new Jsr88Exception(String.format(
                    "There is no module deployed with the ID %s.",
                    ctx.moduleID()));
    }

    void stop() throws Jsr88Exception {
        monitor(CommandType.STOP, dm.stop(targetModuleIDs()));
    }

    void undeploy() throws Jsr88Exception {
        monitor(CommandType.UNDEPLOY, dm.undeploy(targetModuleIDs()));
    }

    void deploy() throws Jsr88Exception {
        monitor(CommandType.DISTRIBUTE, dm.distribute(
                targets(), ctx.moduleArchive(), ctx.deploymentPlan()));
    }

    void start() throws Jsr88Exception {
        monitor(CommandType.START, dm.start(targetModuleIDs()));
    }

    private TargetModuleID[] targetModuleIDs() throws Jsr88Exception {
        final Target[] targets = targets();
        final Collection<TargetModuleID>
                found = new ArrayList<TargetModuleID>(targets.length);
        try {
            final TargetModuleID[] available =
                    dm.getAvailableModules(ctx.moduleType(), targets);
            for (final TargetModuleID tmid : available)
                if (effectiveModuleID.equals(tmid.getModuleID()))
                    found.add(tmid);
        } catch (TargetException ex) {
            throw new AssertionError(ex);
        }
        return found.toArray(new TargetModuleID[found.size()]);
    }

    private Target[] targets() { return dm.getTargets(); }

    private void monitor(final CommandType command, final ProgressObject po)
    throws Jsr88Exception {

        class Monitor implements ProgressListener, Callable<Void> {

            final Logger logger = Logger.getLogger(Jsr88Context.class.getName());

            @Override
            public void handleProgressEvent(final ProgressEvent event) {
                final TargetModuleID tmid = event.getTargetModuleID();
                final String mid = tmid.getModuleID();
                if (null != mid) effectiveModuleID = mid;
                final DeploymentStatus ds = event.getDeploymentStatus();
                final Level level = ds.isFailed()
                        ? Level.WARNING
                        : ds.isCompleted() ? Level.FINE : Level.FINER;
                // Note that ds.getCommand() returns null with
                // org.glassfish.main.deploy:deployment-client:4.0, although it
                // really shouldn't!
                final CommandType command = nonNullOr(ds.getCommand(), command());
                logger.log(level, "{0} command {1,choice,0#is|0<has} {2} with message \"{3}\" on target {4}.",
                        new Object[] {
                            command,
                            ds.getState().getValue(),
                            ds.getState(),
                            ds.getMessage(),
                            tmid.getTarget().getName()
                        });
                synchronized (this) { notifyAll(); }
            }

            @Override public Void call() throws Jsr88Exception {
                po.addProgressListener(this);
                try {
                    while (StateType.RUNNING.equals(state())) {
                        synchronized (this) { wait(); }
                    }
                } catch (InterruptedException ex) {
                    throw new Jsr88Exception(String.format(
                            "Interrupted while waiting for completion of %s command.",
                            command()), ex);
                } finally {
                    po.removeProgressListener(this);
                }
                if (!StateType.COMPLETED.equals(state()))
                    throw new Jsr88Exception(String.format(
                            "Could not complete %s command.", command()));
                return null;
            }

            CommandType command() {
                // Note that deploymentStatus().getCommand() returns null with
                // org.glassfish.main.deploy:deployment-client:4.0, although it
                // really shouldn't!
                return nonNullOr(deploymentStatus().getCommand(), command);
            }

            StateType state() { return deploymentStatus().getState(); }

            DeploymentStatus deploymentStatus() {
                return po.getDeploymentStatus();
            }
        } // Monitor

        new Monitor().call();
    }

    @DischargesObligation @Override public void close() { dm.release(); }
}

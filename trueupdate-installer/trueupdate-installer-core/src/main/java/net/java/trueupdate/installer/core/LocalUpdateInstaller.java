/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.installer.core;

import java.io.*;
import java.util.logging.*;
import javax.annotation.*;
import javax.annotation.concurrent.Immutable;
import net.java.trueupdate.core.zip.JarFileStore;
import net.java.trueupdate.core.zip.patch.ZipPatch;
import static net.java.trueupdate.installer.core.io.Files.*;
import net.java.trueupdate.installer.core.io.PathTask;
import net.java.trueupdate.installer.core.tx.*;
import net.java.trueupdate.installer.core.tx.Transactions.LoggerConfig;
import net.java.trueupdate.manager.spec.*;

/**
 * A local update installer.
 * When updating, this class checks the file system path where the applicatin
 * is currently installed.
 * If the current path is a directory, it assumes that it has been unzipped
 * from the original artifact file (EAR, RAR, WAR etc) and updates the
 * directory accordingly.
 * Otherwise, it assumes that the current path is the original artifact file
 * and updates the file accordingly.
 *
 * @author Christian Schlichtherle
 */
@Immutable
public abstract class LocalUpdateInstaller implements UpdateInstaller {

    private static final Logger logger =
            Logger.getLogger(LocalUpdateInstaller.class.getName());

    private static final LoggerConfig loggerConfig = new LoggerConfig() {
        @Override public Logger logger() { return logger; }
    };

    /**
     * Returns the nullable temporary directory.
     * The implementation in the class {@link LocalUpdateInstaller} always
     * returns {@code null} to indicate that the default temporary directory
     * should be used.
     */
    protected @Nullable File tempDir() { return null; }

    /**
     * Resolves the context for the given update message and location.
     *
     * @param location either {@code message.}{@link UpdateMessage#currentLocation currentLocation}
     *                 or {@code message.}{@link UpdateMessage#updateLocation updateLocation}.
     * @param message the update message as provided to the {@link #install}
     *                method.
     */
    protected abstract Context resolveContext(UpdateMessage message,
                                              String location)
    throws Exception;

    @Override public final void install(final UpdateMessage message,
                                        final File diffZip)
    throws Exception {

        class PatchTask implements PathTask<Void, Exception> {

            final ZipPatch patch;

            PatchTask(final File deployedZip) {
                this.patch = ZipPatch.builder().input(deployedZip).diff(diffZip).build();
            }

            @Override public Void execute(final @WillNotClose File updatedJar)
            throws Exception {
                patch.output(new JarFileStore(updatedJar));
                return null;
            }
        } // PatchTask

        final Context current = resolveContext(message, message.currentLocation());
        final Context update = resolveContext(message, message.updateLocation());

        loanTempDir(new PathTask<Void, Exception>() {
            @Override public Void execute(final File tempDir) throws Exception {
                final File updateJar = new File(tempDir, "updated.jar");
                final File backup = new File(tempDir, "backup");
                if (current.path().isFile()) {
                    Transactions.execute(new CompositeTransaction(
                            timed("to patch the current application file",
                                    new PathTaskTransaction(updateJar, new PatchTask(current.path()))),
                            timed("to undeploy the current application",
                                    undeploymentTransaction(update)),
                            timed("to backup the current application file",
                                    new RenamePathTransaction(update.path(), backup)),
                            timed("to swap-in the updated application file",
                                    new RenamePathTransaction(updateJar, update.path())),
                            timed("to deploy the updated application",
                                    deploymentTransaction(update))));
                } else {
                    final File currentZip = new File(tempDir, "current.zip");
                    final File updateDir = new File(tempDir, "updated.dir");
                    Transactions.execute(new CompositeTransaction(
                            timed("to zip the current application directory",
                                    new ZipTransaction(currentZip, current.path(), "")),
                            timed("to patch the current application file",
                                    new PathTaskTransaction(updateJar, new PatchTask(currentZip))),
                            timed("to unzip the updated application file",
                                    new UnzipTransaction(updateJar, updateDir)),
                            timed("to undeploy the current application",
                                    undeploymentTransaction(update)),
                            timed("to backup the current application directory",
                                    new RenamePathTransaction(update.path(), backup)),
                            timed("to swap-in the updated application directory",
                                    new RenamePathTransaction(updateDir, update.path())),
                            timed("to deploy the updated application", deploymentTransaction(update))));
                }
                return null;
            }
        }, "dir", null, tempDir());
    }

    private static Transaction undeploymentTransaction(Context context) {
        return context.path().exists()
                ? context.undeploymentTransaction()
                : Transactions.noOp();
    }

    private static Transaction deploymentTransaction(Context context) {
        return context.deploymentTransaction();
    }

    private static Transaction timed(String name, Transaction tx) {
        return Transactions.timed(name, tx, loggerConfig);
    }

    /** The context for a location provided to the update installer. */
    public interface Context {

        /** Returns the path of the application. */
        File path();

        /** Returns the transaction for deploying the application. */
        Transaction deploymentTransaction();

        /** Returns the transaction for undeploying the application. */
        Transaction undeploymentTransaction();
    }
}

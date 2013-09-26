/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.installer.core;

import java.io.*;
import java.util.logging.Logger;
import javax.annotation.*;
import javax.annotation.concurrent.Immutable;
import net.java.trueupdate.core.zip.io.JarFileStore;
import net.java.trueupdate.core.zip.patch.ZipPatch;
import static net.java.trueupdate.installer.core.io.Files.*;
import net.java.trueupdate.installer.core.io.PathTask;
import net.java.trueupdate.installer.core.tx.*;
import net.java.trueupdate.manager.spec.*;
import net.java.trueupdate.manager.spec.tx.*;
import net.java.trueupdate.manager.spec.tx.Transactions.LoggerConfig;
import net.java.trueupdate.message.UpdateMessage;

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

    private static final Logger logger = Logger.getLogger(
            LocalUpdateInstaller.class.getName(),
            UpdateMessage.class.getName());

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
     * Resolves the location context for the given update context and location.
     *
     * @param context the update context.
     * @param location either {@code context.}{@link UpdateContext#currentLocation currentLocation}
     *                 or {@code context.}{@link UpdateContext#updateLocation updateLocation}.
     */
    protected abstract LocationContext locationContext(UpdateContext context,
                                                       String location)
    throws Exception;

    @Override
    public final void install(final UpdateContext context) throws Exception {

        class PatchTask implements PathTask<Void, Exception> {

            final ZipPatch patch;

            PatchTask(final File deployedZip) {
                this.patch = ZipPatch
                        .builder()
                        .input(deployedZip)
                        .diff(context.diffZip())
                        .build();
            }

            @Override public Void execute(final @WillNotClose File updatedJar)
            throws Exception {
                patch.output(new JarFileStore(updatedJar));
                return null;
            }
        } // PatchTask

        final LocationContext current =
                locationContext(context, context.currentLocation());
        final LocationContext update =
                locationContext(context, context.updateLocation());

        loanTempDir(new PathTask<Void, Exception>() {

            @Override public Void execute(final File tempDir) throws Exception {
                final File updateJar = new File(tempDir, "updated.jar");
                final File backup = new File(tempDir, "backup");
                if (current.path().isFile()) {
                    Transactions.execute(new CompositeTransaction(
                            timed("lui.patch",
                                    new PathTaskTransaction(updateJar,
                                            new PatchTask(current.path()))),
                            timed("lui.undeploy",
                                    undeploymentTransaction(current, context)),
                            timed("lui.swap.out.file",
                                    new RenamePathTransaction(
                                            update.path(), backup)),
                            timed("lui.swap.in.file",
                                    new RenamePathTransaction(updateJar,
                                            update.path())),
                            timed("lui.deploy",
                                    deploymentTransaction(update))));
                } else {
                    final File currentZip = new File(tempDir, "current.zip");
                    final File updateDir = new File(tempDir, "updated.dir");
                    Transactions.execute(new CompositeTransaction(
                            timed("lui.zip",
                                    new ZipTransaction(currentZip,
                                            current.path(), "")),
                            timed("lui.patch",
                                    new PathTaskTransaction(updateJar,
                                            new PatchTask(currentZip))),
                            timed("lui.unzip",
                                    new UnzipTransaction(updateJar, updateDir)),
                            timed("lui.undeploy",
                                    undeploymentTransaction(current, context)),
                            timed("lui.swap.out.dir",
                                    new RenamePathTransaction(
                                            update.path(), backup)),
                            timed("lui.swap.in.dir",
                                    new RenamePathTransaction(updateDir,
                                            update.path())),
                            timed("lui.deploy",
                                    deploymentTransaction(update))));
                }
                return null;
            }

            Transaction timed(String name, Transaction tx) {
                return Transactions.timed(name, tx, loggerConfig);
            }
        }, "dir", null, tempDir());
    }

    static Transaction undeploymentTransaction(
            final LocationContext location,
            final UpdateContext update) {

        class Handshake extends Transaction {
            final Transaction tx = location.undeploymentTransaction();

            @Override public void prepare() throws Exception {
                tx.prepare();
                update.prepareUndeployment();
            }

            @Override public void perform() throws Exception {
                tx.perform();
                update.performUndeployment();
            }

            @Override public void rollback() throws Exception {
                tx.rollback();
                update.rollbackUndeployment();
            }

            @Override public void commit() throws Exception {
                tx.commit();
                update.commitUndeployment();
            }
        } // Handshake

        return location.path().exists() ? new Handshake() : Transactions.noOp();
    }

    static Transaction deploymentTransaction(LocationContext context) {
        return context.deploymentTransaction();
    }

    /** The context for a location provided to the update installer. */
    public interface LocationContext {

        /** Returns the path of the application. */
        File path();

        /** Returns the transaction for undeploying the application. */
        Transaction undeploymentTransaction();

        /** Returns the transaction for deploying the application. */
        Transaction deploymentTransaction();
    }
}

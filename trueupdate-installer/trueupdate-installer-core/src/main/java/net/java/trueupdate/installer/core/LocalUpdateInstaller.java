/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.installer.core;

import java.io.*;
import javax.annotation.*;
import javax.annotation.concurrent.Immutable;
import net.java.trueupdate.core.zip.io.JarFileStore;
import net.java.trueupdate.core.zip.patch.ZipPatch;
import static net.java.trueupdate.installer.core.io.Files.*;
import net.java.trueupdate.installer.core.io.PathTask;
import net.java.trueupdate.installer.core.tx.*;
import net.java.trueupdate.manager.spec.*;
import static net.java.trueupdate.manager.spec.Action.*;
import net.java.trueupdate.manager.spec.tx.*;

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
                            decorate(PATCH,
                                    new PathTaskTransaction(updateJar,
                                            new PatchTask(current.path()))),
                            decorate(UNDEPLOY,
                                    current.undeploymentTransaction()),
                            decorate(SWAP_OUT_FILE,
                                    new RenamePathTransaction(
                                            current.path(), backup)),
                            decorate(SWAP_IN_FILE,
                                    new RenamePathTransaction(updateJar,
                                            update.path())),
                            decorate(DEPLOY,
                                    update.deploymentTransaction())));
                } else {
                    final File currentZip = new File(tempDir, "current.zip");
                    final File updateDir = new File(tempDir, "updated.dir");
                    Transactions.execute(new CompositeTransaction(
                            decorate(ZIP,
                                    new ZipTransaction(currentZip,
                                            current.path(), "")),
                            decorate(PATCH,
                                    new PathTaskTransaction(updateJar,
                                            new PatchTask(currentZip))),
                            decorate(UNZIP,
                                    new UnzipTransaction(updateJar, updateDir)),
                            decorate(UNDEPLOY,
                                    current.undeploymentTransaction()),
                            decorate(SWAP_OUT_DIR,
                                    new RenamePathTransaction(
                                            current.path(), backup)),
                            decorate(SWAP_IN_DIR,
                                    new RenamePathTransaction(updateDir,
                                            update.path())),
                            decorate(DEPLOY,
                                    update.deploymentTransaction())));
                }
                return null;
            }

            Transaction decorate(Action id, Transaction tx) {
                return context.decorate(id, tx);
            }
        }, "dir", null, tempDir());
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

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
 * A basic update installer.
 * When updating, this class checks the file system path where the application
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
public abstract class CoreUpdateInstaller implements UpdateInstaller {

    /**
     * Returns the nullable temporary directory.
     * The implementation in the class {@link CoreUpdateInstaller} always
     * returns {@code null} to indicate that the default temporary directory
     * should be used.
     */
    protected @Nullable File tempDir() { return null; }

    /** Derives the location context from the given update context. */
    protected abstract LocationContext locationContext(UpdateContext context)
    throws Exception;

    @Override
    public final void install(final UpdateContext uc) throws Exception {

        class PatchTask implements PathTask<Void, Exception> {

            final ZipPatch patch;

            PatchTask(final File currentZip) {
                this.patch = ZipPatch
                        .builder()
                        .input(currentZip)
                        .delta(uc.deltaZip())
                        .build();
            }

            @Override public Void execute(final @WillNotClose File updatedJar)
            throws Exception {
                patch.output(new JarFileStore(updatedJar));
                return null;
            }
        } // PatchTask

        final LocationContext lc = locationContext(uc);

        loanTempDir(new PathTask<Void, Exception>() {

            @Override public Void execute(final File tempDir) throws Exception {
                final File updatedJar = new File(tempDir, "updated.jar");
                final File backup = new File(tempDir, "backup");
                if (lc.currentPath().isFile()) {
                    Transactions.execute(new CompositeTransaction(
                            decorate(PATCH,
                                    new PathTaskTransaction(updatedJar,
                                            new PatchTask(lc.currentPath()))),
                            decorate(UNDEPLOY,
                                    lc.undeploymentTransaction()),
                            decorate(SWAP_OUT_FILE,
                                    new RenamePathTransaction(
                                            lc.currentPath(), backup)),
                            decorate(SWAP_IN_FILE,
                                    new RenamePathTransaction(updatedJar,
                                            lc.updatePath())),
                            decorate(DEPLOY,
                                    lc.deploymentTransaction())));
                } else {
                    final File currentZip = new File(tempDir, "current.zip");
                    final File updatedDir = new File(tempDir, "updated.dir");
                    Transactions.execute(new CompositeTransaction(
                            decorate(ZIP,
                                    new ZipTransaction(currentZip,
                                            lc.currentPath(), "")),
                            decorate(PATCH,
                                    new PathTaskTransaction(updatedJar,
                                            new PatchTask(currentZip))),
                            decorate(UNZIP,
                                    new UnzipTransaction(updatedJar, updatedDir)),
                            decorate(UNDEPLOY,
                                    lc.undeploymentTransaction()),
                            decorate(SWAP_OUT_DIR,
                                    new RenamePathTransaction(
                                            lc.currentPath(), backup)),
                            decorate(SWAP_IN_DIR,
                                    new RenamePathTransaction(updatedDir,
                                            lc.updatePath())),
                            decorate(DEPLOY,
                                    lc.deploymentTransaction())));
                }
                return null;
            }

            Transaction decorate(Action id, Transaction tx) {
                return uc.decorate(id, tx);
            }
        }, "dir", null, tempDir());
    }

    /** The context for a location provided to the update installer. */
    public interface LocationContext {

        /** Returns the current path of the application. */
        File currentPath();

        /** Returns the update path of the application. */
        File updatePath();

        /**
         * Returns the transaction for undeploying the application at the
         * current path.
         */
        Transaction undeploymentTransaction();

        /**
         * Returns the transaction for deploying the application at the
         * update path.
         */
        Transaction deploymentTransaction();
    }
}

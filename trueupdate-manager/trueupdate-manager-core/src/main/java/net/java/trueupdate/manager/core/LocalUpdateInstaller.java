/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.core;

import java.io.*;
import java.net.URI;
import java.util.logging.*;
import javax.annotation.*;
import net.java.trueupdate.core.zip.JarFileStore;
import net.java.trueupdate.core.zip.patch.ZipPatch;
import static net.java.trueupdate.manager.core.io.Files.*;
import net.java.trueupdate.manager.core.io.PathTask;
import net.java.trueupdate.manager.core.tx.*;
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
public abstract class LocalUpdateInstaller implements UpdateInstaller {

    private static final Logger logger =
            Logger.getLogger(LocalUpdateInstaller.class.getName());

    /**
     * Returns the nullable temporary directory.
     * The implementation in the class {@link LocalUpdateInstaller} always
     * returns {@code null} to indicate that the default temporary directory
     * should be used.
     */
    protected @CheckForNull File tempDir() { return null; }

    /** Returns the path of the deployed application. */
    protected abstract File resolvePath(URI location) throws Exception;

    /** Returns the transaction for deploying the application. */
    protected abstract Transaction deploymentTx(File path);

    /** Returns the transaction for undeploying the application. */
    protected abstract Transaction undeploymentTx(File path);

    @Override public final void install(final UpdateResolver resolver,
                                        final UpdateMessage message)
    throws Exception {

        final File diffZip = resolveZipDiff(resolver, message.updateDescriptor());

        class PatchTask implements TransactionTask {

            final ZipPatch patch;

            PatchTask(final File deployedZip) {
                this.patch = ZipPatch.builder().input(deployedZip).diff(diffZip).build();
            }

            @Override public Void execute(final @WillNotClose File updatedJar) throws Exception {
                patch.output(new JarFileStore(updatedJar));
                return null;
            }
        } // PatchTask

        final URI currentLocation = message.currentLocation();
        final File currentPath = resolvePath(currentLocation);
        final URI updateLocation = message.updateLocation();
        final File updatePath = resolvePath(updateLocation);

        loanTempPath(new TransactionTask() {
            @Override public Void execute(final File backupPath) throws Exception {
                return loanTempPath(new TransactionTask() {
                    @Override public Void execute(final File updateJar) throws Exception {
                        if (currentPath.isFile()) {
                            Transactions.execute(new CompositeTransaction(
                                    new PathTaskTransaction(updateJar, new PatchTask(currentPath)),
                                    undeploymentTx(updatePath),
                                    new RenamePathTransaction(updatePath, backupPath),
                                    new RenamePathTransaction(updateJar, updatePath),
                                    deploymentTx(updatePath)));
                        } else {
                            return loanTempPath(new TransactionTask() {
                                @Override public Void execute(final File updateDir) throws Exception {
                                    return loanTempPath(new TransactionTask() {
                                        @Override public Void execute(final File currentZip) throws Exception {
                                            Transactions.execute(new CompositeTransaction(
                                                    new ZipTransaction(currentZip, currentPath, ""),
                                                    new PathTaskTransaction(updateJar, new PatchTask(currentZip)),
                                                    new UnzipTransaction(updateJar, updateDir),
                                                    undeploymentTx(updatePath),
                                                    new RenamePathTransaction(updatePath, backupPath),
                                                    new RenamePathTransaction(updateDir, updatePath),
                                                    deploymentTx(updatePath)));
                                            return null;
                                        }
                                    }, "current", ".zip");
                                }
                            }, "update", ".dir");
                        }
                        return null;
                    }
                }, "update", ".jar");
            }
        }, "backup", ".path");
    }

    private static File resolveZipDiff(final UpdateResolver resolver,
                                       final UpdateDescriptor descriptor)
    throws Exception {
        final File diffZip = resolver.resolveZipDiffFile(descriptor);
        logger.log(Level.FINER,
                "Resolved ZIP diff file {0} for artifact descriptor {1} and update version {2} .",
                new Object[] { diffZip, descriptor.artifactDescriptor(),
                               descriptor.updateVersion() });
        return diffZip;
    }

    private Void loanTempPath(
            final TransactionTask task,
            final String prefix,
            final String suffix)
    throws Exception {
        class DeleteAndForwardTask implements TransactionTask {
            @Override public Void execute(final File path) throws Exception {
                deletePath(path);
                return task.execute(path);
            }
        }
        return loanTempFile(new DeleteAndForwardTask(), prefix, suffix, tempDir());
    }

    private interface TransactionTask extends PathTask<Void, Exception> { }
}

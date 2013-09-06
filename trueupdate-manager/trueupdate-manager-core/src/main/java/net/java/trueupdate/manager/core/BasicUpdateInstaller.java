/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.core;

import java.io.*;
import java.util.logging.*;
import javax.annotation.*;
import net.java.trueupdate.core.zip.JarFileStore;
import net.java.trueupdate.core.zip.patch.ZipPatch;
import static net.java.trueupdate.manager.core.io.Files.*;
import net.java.trueupdate.manager.core.io.PathTask;
import net.java.trueupdate.manager.core.tx.*;
import net.java.trueupdate.manager.spec.*;

/**
 * A basic update installer.
 * When updating, this class checks the deployed path.
 * If the deployed path is a directory, it assumes that it has been unzipped
 * from the original artifact file (EAR, RAR, WAR etc) and updates the
 * directory accordingly.
 * Otherwise, it assumes that the deployed path is the original artifact file
 * and updates the file accordingly.
 *
 * @author Christian Schlichtherle
 */
public abstract class BasicUpdateInstaller implements UpdateInstaller {

    private static final Logger logger =
            Logger.getLogger(BasicUpdateInstaller.class.getName());

    /**
     * Returns the nullable temporary directory.
     * The implementation in the class {@link BasicUpdateInstaller} always
     * returns {@code null} to indicate that the default temporary directory
     * should be used.
     */
    protected @CheckForNull File tempDir() { return null; }

    /** Returns the path of the deployed application. */
    protected abstract File resolveDeployedPath(UpdateMessage message);

    /** Returns the transaction for deploying the application. */
    protected abstract Transaction deploymentTx();

    /** Returns the transaction for undeploying the application. */
    protected abstract Transaction undeploymentTx();

    @Override public void install(final UpdateResolver resolver, final UpdateMessage message) throws Exception {

        final File deployedPath = resolveDeployedPath(message);
        final File diffZip = resolveZipDiff(resolver, message);

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

        loanTempPath(new TransactionTask() {
            @Override public Void execute(final File backupPath) throws Exception {
                return loanTempPath(new TransactionTask() {
                    @Override public Void execute(final File updatedJar) throws Exception {
                        if (deployedPath.isFile()) {
                            Transactions.execute(new CompositeTransaction(
                                    new PathTaskTransaction(updatedJar, new PatchTask(deployedPath)),
                                    undeploymentTx(),
                                    new RenamePathTransaction(deployedPath, backupPath),
                                    new RenamePathTransaction(updatedJar, deployedPath),
                                    deploymentTx()));
                        } else {
                            return loanTempPath(new TransactionTask() {
                                @Override public Void execute(final File updatedDir) throws Exception {
                                    return loanTempPath(new TransactionTask() {
                                        @Override public Void execute(final File deployedZip) throws Exception {
                                            Transactions.execute(new CompositeTransaction(
                                                    new ZipTransaction(deployedZip, deployedPath, ""),
                                                    new PathTaskTransaction(updatedJar, new PatchTask(deployedZip)),
                                                    new UnzipTransaction(updatedJar, updatedDir),
                                                    undeploymentTx(),
                                                    new RenamePathTransaction(deployedPath, backupPath),
                                                    new RenamePathTransaction(updatedDir, deployedPath),
                                                    deploymentTx()));
                                            return null;
                                        }
                                    }, "deployed", ".zip");
                                }
                            }, "updated", ".dir");
                        }
                        return null;
                    }
                }, "updated", ".jar");
            }
        }, "backup", ".path");
    }

    private static File resolveZipDiff(final UpdateResolver resolver, final UpdateMessage message) throws Exception {
        final UpdateDescriptor updateDescriptor = message.updateDescriptor();
        final File diffZip = resolver.resolveZipDiffFile(updateDescriptor);
        logger.log(Level.FINER,
                "Resolved ZIP diff file {0} for artifact descriptor {1} and update version {2} .",
                new Object[] { diffZip, updateDescriptor.artifactDescriptor(),
                               updateDescriptor.updateVersion() });
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

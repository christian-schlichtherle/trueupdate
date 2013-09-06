/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.core;

import java.io.*;
import java.net.URI;
import java.util.logging.*;
import javax.annotation.*;
import javax.annotation.concurrent.Immutable;
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
@Immutable
public abstract class LocalUpdateInstaller implements UpdateInstaller {

    private static final Logger logger =
            Logger.getLogger(LocalUpdateInstaller.class.getName());

    /**
     * Returns the nullable temporary directory.
     * The implementation in the class {@link LocalUpdateInstaller} always
     * returns {@code null} to indicate that the default temporary directory
     * should be used.
     */
    protected @Nullable File tempDir() { return null; }

    /**
     * Resolves the context for the given location.
     *
     * @param location either the current location or the update location as
     *                 defined by the update message parameter for the method
     *                 {@link #install}.
     */
    protected abstract Context resolveContext(URI location) throws Exception;

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

        final Context current = resolveContext(message.currentLocation());
        final Context update = resolveContext(message.updateLocation());

        loanTempPath(new TransactionTask() {
            @Override public Void execute(final File backupPath) throws Exception {
                return loanTempPath(new TransactionTask() {
                    @Override public Void execute(final File updateJar) throws Exception {
                        if (current.path().isFile()) {
                            Transactions.execute(new CompositeTransaction(
                                    new PathTaskTransaction(updateJar, new PatchTask(current.path())),
                                    undeploymentTx(update),
                                    new RenamePathTransaction(update.path(), backupPath),
                                    new RenamePathTransaction(updateJar, update.path()),
                                    deploymentTx(update)));
                        } else {
                            return loanTempPath(new TransactionTask() {
                                @Override public Void execute(final File updateDir) throws Exception {
                                    return loanTempPath(new TransactionTask() {
                                        @Override public Void execute(final File currentZip) throws Exception {
                                            Transactions.execute(new CompositeTransaction(
                                                    new ZipTransaction(currentZip, current.path(), ""),
                                                    new PathTaskTransaction(updateJar, new PatchTask(currentZip)),
                                                    new UnzipTransaction(updateJar, updateDir),
                                                    undeploymentTx(update),
                                                    new RenamePathTransaction(update.path(), backupPath),
                                                    new RenamePathTransaction(updateDir, update.path()),
                                                    deploymentTx(update)));
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

    private static Transaction undeploymentTx(Context context) {
        return context.path().exists()
                ? context.undeploymentTx()
                : new Transaction() {
                    @Override protected void perform() throws Exception { }
                    @Override protected void rollback() throws Exception { }
                };
    }

    private static Transaction deploymentTx(Context context) {
        return context.deploymentTx();
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

    /** The context for a location provided to the update installer. */
    public interface Context {

        /** Returns the path of the application. */
        File path();

        /** Returns the transaction for deploying the application. */
        Transaction deploymentTx();

        /** Returns the transaction for undeploying the application. */
        Transaction undeploymentTx();
    }
}

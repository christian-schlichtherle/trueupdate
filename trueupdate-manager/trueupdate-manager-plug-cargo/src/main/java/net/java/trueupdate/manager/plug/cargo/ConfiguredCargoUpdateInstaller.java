/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.plug.cargo;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.logging.*;
import javax.annotation.concurrent.Immutable;
import net.java.trueupdate.artifact.spec.*;
import net.java.trueupdate.core.zip.patch.ZipPatch;
import net.java.trueupdate.manager.core.UpdateResolver;
import net.java.trueupdate.manager.core.io.*;
import static net.java.trueupdate.manager.core.io.Files.*;
import net.java.trueupdate.manager.core.tx.*;
import net.java.trueupdate.manager.spec.*;
import org.codehaus.cargo.container.deployable.*;

/**
 * Installs updates for applications running in OpenEJB.
 *
 * @author Christian Schlichtherle
 */
@Immutable
class ConfiguredCargoUpdateInstaller {

    private static final Logger logger =
            Logger.getLogger(ConfiguredCargoUpdateInstaller.class.getName());

    private final UpdateMessage message;
    private final CargoContext cargoContext;

    ConfiguredCargoUpdateInstaller(final UpdateMessage message) {
        this.cargoContext = new CargoContext(message.currentLocation());
        this.message = message;
    }

    void install(final UpdateResolver resolver) throws Exception {
        final Deployable deployable = cargoContext.deployable();
        if (!deployable.isExpanded())
            throw new Exception("Deployment of a file (EAR, RAR, WAR etc) is not supported yet - please use an expanded directory.");
        final File deploymentDir = new File(deployable.getFile());
        assert deploymentDir.isDirectory();
        logger.log(Level.FINE,
                "Resolved current location {0} to deployment directory {1} .",
                new Object[] { currentLocation(), deploymentDir });
        final File patchFile = resolver.resolveZipDiffFile(updateDescriptor());
        logger.log(Level.FINER,
                "Resolved patch ZIP file {0} for artifact descriptor {1} and update version {2} .",
                new Object[] { patchFile, artifactDescriptor(),
                               updateVersion() });

        class RedeployTask implements PathTask<Void, Exception> {
            @Override public Void execute(final File patchedJarFile) throws Exception {
                final File updateDir = createTempPath("update");
                final File backupDir = createTempPath("backup");

                class DeployTransaction extends Transaction {

                    @Override public void perform() throws Exception {
                        cargoContext.deploy();
                    }

                    @Override public void rollback() throws Exception {
                        cargoContext.undeploy();
                    }
                } // DeployCommand

                class UndeployTransaction extends Transaction {

                    @Override public void perform() throws Exception {
                        cargoContext.undeploy();
                    }

                    @Override public void rollback() throws Exception {
                        cargoContext.deploy();
                    }
                } // UndeployCommand

                Transactions.execute(new CompositeTransaction(
                        new UnzipTransaction(patchedJarFile, updateDir),
                        new UndeployTransaction(),
                        new RenamePathTransaction(deploymentDir, backupDir),
                        new RenamePathTransaction(updateDir, deploymentDir),
                        new DeployTransaction()));
                try {
                    deletePath(backupDir);
                } catch (final IOException ex) {
                    if (logger.isLoggable(Level.WARNING))
                        logger.log(Level.WARNING, String.format("Cannot delete backup directory %s :", backupDir), ex);
                }
                return null;
            }
        } // RedeployTask

        class PatchTask implements PathTask<Void, Exception> {
            @Override public Void execute(final File inputFile) throws Exception {
                loanPatchedFile(new RedeployTask(), inputFile, patchFile);
                return null;
            }
        } // PatchTask

        loanInputFile(new PatchTask(), deploymentDir);
    }

    private static void loanInputFile(
            final PathTask<Void, Exception> task,
            final File deploymentDir)
            throws Exception {

        class MakeInputFile implements PathTask<Void, Exception> {
            @Override public Void execute(final File inputFile) throws Exception {
                zip(inputFile, deploymentDir);
                logger.log(Level.FINER,
                        "Rebuilt input ZIP file {0} from deployment directory {1} .",
                        new Object[] { inputFile, deploymentDir });
                return task.execute(inputFile);
            }
        } // MakeInputFile

        loanTempFile(new MakeInputFile(), "input", ".zip");
    }

    private static void loanPatchedFile(
            final PathTask<Void, Exception> task,
            final File inputFile,
            final File patchFile)
    throws Exception {

        class MakePatchedFile implements PathTask<Void, Exception> {
            @Override public Void execute(final File patchedFile) throws Exception {
                ZipPatch.builder().input(inputFile).diff(patchFile).build().output(patchedFile);
                logger.log(Level.FINER,
                        "Patched input ZIP file {0} with patch ZIP file {1} to patched JAR file {2} .",
                        new Object[] { inputFile, patchFile, patchedFile });
                return task.execute(patchedFile);
            }
        } // MakePatchedFile

        loanTempFile(new MakePatchedFile(), "patched", ".jar");
    }

    private ArtifactDescriptor artifactDescriptor() {
        return updateDescriptor().artifactDescriptor();
    }

    private String updateVersion() {
        return updateDescriptor().updateVersion();
    }

    private UpdateDescriptor updateDescriptor() {
        return message.updateDescriptor();
    }

    private URI currentLocation() {
        return message.currentLocation();
    }
}

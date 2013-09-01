/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.plug.cargo;

import java.io.*;
import java.io.File;
import java.net.URI;
import java.util.logging.*;
import javax.annotation.concurrent.Immutable;
import net.java.trueupdate.artifact.spec.*;
import net.java.trueupdate.manager.core.UpdateResolver;
import static net.java.trueupdate.manager.plug.cargo.Files.*;
import net.java.trueupdate.manager.plug.cargo.Files.FileTask;
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
            throw new Exception("Deployment of a file (EAR, RAR, WAR et al) is not supported yet - please use an expanded directory.");
        final File deploymentDir = new File(deployable.getFile());
        assert deploymentDir.isDirectory();
        logger.log(Level.FINE,
                "Resolved current location {0} to deployment directory {1} .",
                new Object[] { currentLocation(), deploymentDir });
        final File patchFile = resolver.resolveZipPatchFile(updateDescriptor());
        logger.log(Level.FINER,
                "Resolved patch ZIP file {0} for artifact descriptor {1} and update version {2} .",
                new Object[] { patchFile, artifactDescriptor(),
                               updateVersion() });

        class RedeployTask implements FileTask {

            @Override
            public void execute(final File patchedJarFile) throws Exception {
                final File updateDir = createEmptySlot("update", ".dir");
                final File backupDir = createEmptySlot("backup", ".dir");

                class UnjarUpdateCommand implements Command {

                    @Override public void execute() throws Exception {
                        unzipTo(patchedJarFile, updateDir);
                    }

                    @Override public void revert() throws Exception {
                        if (!deleteAll(updateDir))
                            throw new IOException(String.format(
                                    "Cannot delete update directory %s .",
                                    updateDir));
                    }
                } // UnjarUpdateCommand

                class DeployCommand implements Command {

                    @Override public void execute() throws Exception {
                        cargoContext.deploy();
                    }

                    @Override public void revert() throws Exception {
                        cargoContext.undeploy();
                    }
                } // DeployCommand

                class DeleteBackupCommand implements Command {

                    @Override public void execute() throws Exception {
                        if (!deleteAll(backupDir))
                            throw new IOException(String.format(
                                    "Cannot delete backup directory %s .",
                                    backupDir));
                    }

                    @Override public void revert() throws Exception {
                        throw new AssertionError(
                                "This must be the last command and hence there is no need to ever revert it.");
                    }
                } // DeleteBackupCommand

                new Transaction(
                        new UnjarUpdateCommand(),
                        new InverseCommand(new DeployCommand()),
                        new RenameFileCommand(deploymentDir, backupDir),
                        new RenameFileCommand(updateDir, deploymentDir),
                        new DeployCommand(),
                        new DeleteBackupCommand()
                        ).execute();
            }
        } // RedeployTask

        class PatchTask implements FileTask {
            @Override
            public void execute(final File inputFile) throws Exception {
                loanPatchedFile(new RedeployTask(), inputFile, patchFile);
            }
        } // PatchTask

        loanInputFile(new PatchTask(), deploymentDir);
    }

    private static void loanInputFile(
            final FileTask task,
            final File deploymentDir)
            throws Exception {

        class MakeInputFile implements FileTask {

            @Override public void execute(final File inputFile) throws Exception {
                zipTo(deploymentDir, inputFile);
                logger.log(Level.FINER,
                        "Rebuilt input ZIP file {0} from deployment directory {1} .",
                        new Object[] { inputFile, deploymentDir });
                task.execute(inputFile);
            }
        } // MakeInputFile

        loanTempFile(new MakeInputFile(), "input", ".zip");
    }

    private static void loanPatchedFile(
            final FileTask task,
            final File inputFile,
            final File patchFile)
    throws Exception {

        class MakePatchedFile implements FileTask {

            @Override
            public void execute(final File patchedFile) throws Exception {
                applyPatchTo(inputFile, patchFile, patchedFile);
                logger.log(Level.FINER,
                        "Patched input ZIP file {0} with patch ZIP file {1} to patched JAR file {2} .",
                        new Object[] { inputFile, patchFile, patchedFile });
                task.execute(patchedFile);
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

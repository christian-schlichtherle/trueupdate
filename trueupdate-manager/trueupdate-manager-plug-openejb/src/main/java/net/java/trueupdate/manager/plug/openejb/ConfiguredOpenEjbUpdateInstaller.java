/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.plug.openejb;

import java.io.*;
import java.net.URI;
import java.util.logging.*;
import javax.annotation.concurrent.Immutable;
import net.java.trueupdate.artifact.spec.*;
import net.java.trueupdate.manager.core.UpdateResolver;
import static net.java.trueupdate.manager.plug.openejb.Files.*;
import net.java.trueupdate.manager.plug.openejb.Files.FileTask;
import net.java.trueupdate.manager.spec.*;
import static net.java.trueupdate.shed.Objects.*;
import org.apache.openejb.assembler.Deployer;
import org.apache.openejb.assembler.classic.AppInfo;

/**
 * Installs updates for applications running in OpenEJB.
 *
 * @author Christian Schlichtherle
 */
@Immutable
class ConfiguredOpenEjbUpdateInstaller {

    private static final Logger logger =
            Logger.getLogger(ConfiguredOpenEjbUpdateInstaller.class.getName());

    private final Deployer deployer;
    private final UpdateMessage message;

    ConfiguredOpenEjbUpdateInstaller(
            final Deployer deployer,
            final UpdateMessage message) {
        this.deployer = requireNonNull(deployer);
        this.message = requireNonNull(message);
    }

    void install(final UpdateResolver resolver) throws Exception {
        final AppInfo info = resolveAppInfo();
        final File deploymentDir = new File(info.path);
        logger.log(Level.FINE, "Resolved current location {0} to deployment directory {1} .",
                new Object[] { currentLocation(), deploymentDir });
        final File zipPatchFile = resolver.resolveZipPatchFile(updateDescriptor());
        logger.log(Level.FINER, "Resolved ZIP patch file {0} for artifact descriptor {1} and update version {2} .",
                new Object[] { zipPatchFile, artifactDescriptor(),
                               updateVersion() });

        class RedeployTask implements FileTask {

            @Override
            public void execute(final File patchedJarFile) throws Exception {
                final File updateDir = createTempSlotForSibling(deploymentDir);
                final File backupDir = createTempSlotForSibling(deploymentDir);

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

                final class DeployCommand implements Command {

                    @Override public void execute() throws Exception {
                        deployer.deploy(deploymentDir.getPath());
                    }

                    @Override public void revert() throws Exception {
                        deployer.undeploy(deploymentDir.getPath());
                    }
                } // DeployCommand

                final class DeleteBackupCommand implements Command {

                    @Override public void execute() throws Exception {
                        if (!deleteAll(backupDir))
                            throw new IOException(String.format(
                                    "Cannot delete backup directory %s .",
                                    backupDir));
                    }

                    @Override public void revert() throws Exception {
                        throw new AssertionError("This must be the last command and hence there is no need to ever revert it.");
                    }
                } // DeleteBackupCommand

                new MacroCommand(
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
            public void execute(final File inputZipFile) throws Exception {
                loanPatchedJarFile(new RedeployTask(), inputZipFile, zipPatchFile);
            }
        } // PatchTask

        loanInputZipFile(new PatchTask(), deploymentDir);
    }

    private AppInfo resolveAppInfo() throws FileNotFoundException {
        final URI location = currentLocation();
        final Scheme scheme = Scheme.valueOf(location.getScheme());
        for (final AppInfo info : deployer.getDeployedApps())
            if (scheme.matches(location, info))
                return info;
        throw new FileNotFoundException(
                String.format("Cannot resolve application information for %s .", location));
    }

    private static void loanPatchedJarFile(
            final FileTask task,
            final File inputZipFile,
            final File zipPatchFile)
    throws Exception {

        class MakePatchedJarFile implements FileTask {

            @Override
            public void execute(final File patchedJarFile) throws Exception {
                applyPatchTo(inputZipFile, zipPatchFile, patchedJarFile);
                logger.log(Level.FINER, "Patched JAR file {0} with ZIP patch file {1} to JAR file {2} .",
                        new Object[] { inputZipFile, zipPatchFile, patchedJarFile });
                task.execute(patchedJarFile);
            }
        } // MakePatchedJarFile

        loanTempFileTo("output", ".jar", new MakePatchedJarFile());
    }

    private static void loanInputZipFile(
            final FileTask task,
            final File deploymentDir)
    throws Exception {

        class MakeInputZipFile implements FileTask {

            @Override public void execute(final File file) throws Exception {
                zipTo(deploymentDir, file);
                logger.log(Level.FINER, "Rebuilt original JAR file {0} from directory {1} .",
                        new Object[] { file, deploymentDir });
                task.execute(file);
            }
        } // MakeOriginalJarFile

        loanTempFileTo("input", ".jar", new MakeInputZipFile());
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

enum Scheme {
    app {
        @Override boolean matches(URI location, AppInfo info) {
            return location.getSchemeSpecificPart().equals(info.appId);
        }
    },

    file {
        @Override boolean matches(URI location, AppInfo info) {
            return new File(location).equals(new File(info.path));
        }
    };

    abstract boolean matches(URI location, AppInfo info);
}

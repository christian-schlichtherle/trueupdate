/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.plug.openejb;

import java.io.*;
import java.net.URI;
import java.util.*;
import java.util.jar.*;
import java.util.logging.*;
import java.util.regex.Pattern;
import java.util.zip.*;
import javax.annotation.concurrent.Immutable;
import net.java.trueupdate.artifact.spec.*;
import net.java.trueupdate.core.io.*;
import net.java.trueupdate.core.zip.patch.ZipPatch;
import net.java.trueupdate.manager.core.UpdateResolver;
import net.java.trueupdate.manager.spec.*;
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

    private static final Pattern ZIP_EXTENSIONS =
            Pattern.compile(".*\\.(ear|jar|war|zip)", Pattern.CASE_INSENSITIVE);

    private final Deployer deployer;
    private final UpdateMessage message;

    ConfiguredOpenEjbUpdateInstaller(
            final Deployer deployer,
            final UpdateMessage message) {
        this.deployer = Objects.requireNonNull(deployer);
        this.message = Objects.requireNonNull(message);
    }

    void install(final UpdateResolver resolver) throws Exception {
        final AppInfo info = resolveAppInfo();
        final File directory = new File(info.path);
        logger.log(Level.FINE, "Resolved current location {0} to installation directory {1} .",
                new Object[] { currentLocation(), directory });
        final File zipPatchFile = resolver.resolveZipPatchFile(updateDescriptor());
        logger.log(Level.FINER, "Resolved ZIP patch file {0} for artifact descriptor {1} and update version {2} .",
                new Object[] { zipPatchFile, artifactDescriptor(),
                               updateVersion() });

        class RedeployTask implements FileTask {
            @Override
            public void process(final File patchedJarFile) throws Exception {
                deployer.undeploy(info.path);
                deployer.deploy(patchedJarFile.getPath());
            }
        } // RedeployTask

        class PatchTask implements FileTask {
            @Override
            public void process(final File originalJarFile) throws Exception {
                withPatchedJarFile(originalJarFile, zipPatchFile, new RedeployTask());
            }
        } // PatchTask

        withOriginalJarFile(directory, new PatchTask());
    }

    private AppInfo resolveAppInfo() throws FileNotFoundException {
        final URI location = currentLocation();
        final Scheme scheme = Scheme.valueOf(location.getScheme());
        for (final AppInfo info : deployer.getDeployedApps())
            if (scheme.matches(location, info))
                return info;
        throw new FileNotFoundException(
                String.format("Cannot locate installation directory of %s .", location));
    }

    private void withPatchedJarFile(
            final File originalJarFile,
            final File zipPatchFile,
            final FileTask task)
    throws Exception {

        class MakePatchedJarFile implements FileTask {
            @Override
            public void process(File patchedJarFile) throws Exception {
                applyPatchTo(originalJarFile, zipPatchFile, patchedJarFile);
                logger.log(Level.FINER, "Patched JAR file {0} with ZIP patch file {1} to JAR file {2} .",
                        new Object[] { originalJarFile, zipPatchFile, patchedJarFile });
                task.process(patchedJarFile);
            }
        } // MakePatchedJarFile

        withTempFile("output", ".jar", new MakePatchedJarFile());
    }

    private void withOriginalJarFile(
            final File directory,
            final FileTask task)
    throws Exception {

        class MakeOriginalJarFile implements FileTask {
            @Override
            public void process(final File file) throws Exception {
                jarTo(directory, file);
                logger.log(Level.FINER, "Rebuilt original JAR file {0} from directory {1} .",
                        new Object[] { file, directory });
                task.process(file);
            }
        } // MakeOriginalJarFile

        withTempFile("input", ".jar", new MakeOriginalJarFile());
    }

    private void withTempFile(
            final String prefix,
            String suffix,
            final FileTask task)
    throws Exception {
        final File temp = File.createTempFile(prefix, ".jar");
        try {
            logger.log(Level.FINEST, "Created temporary file {0} .", temp);
            task.process(temp);
        } finally {
            if (temp.delete()) {
                logger.log(Level.FINEST, "Deleted temporary file {0} .", temp);
            } else {
                logger.log(Level.WARNING, "Cannot delete temporary file {0} .", temp);
            }
        }
    }

    private static void applyPatchTo(
            final File originalJarFile,
            final File zipPatchFile,
            final File patchedJarFile)
    throws IOException {
        try (JarFile originalJar = new JarFile(originalJarFile);
             ZipFile zipPatch = new ZipFile(zipPatchFile)) {
            ZipPatch.builder()
                    .inputZipFile(originalJar)
                    .zipPatchFile(zipPatch)
                    .outputJarFile(true)
                    .build()
                    .applyZipPatchFileTo(new FileStore(patchedJarFile));
        }
    }

    private static void jarTo(final File directory, final File jarFile)
    throws IOException {
        assert directory.isDirectory();
        try (JarOutputStream
                out = new JarOutputStream(new FileOutputStream(jarFile))) {

            class Jar  {
                void jar(final File directory, final String name)
                throws IOException {
                    final File[] memberFiles = directory.listFiles();
                    Arrays.sort(memberFiles); // courtesy
                    for (final File memberFile : memberFiles) {
                        final String memberName =
                                (name.isEmpty() ? name : name + '/')
                                + memberFile.getName();
                        if (memberFile.isDirectory()) {
                            addDirectoryEntry(memberName);
                            jar(memberFile, memberName);
                        } else {
                            addFileEntry(memberName, memberFile);
                        }
                    }
                }

                void addDirectoryEntry(final String name) throws IOException {
                    final ZipEntry entry = entry(name + '/');
                    entry.setMethod(ZipOutputStream.STORED);
                    entry.setSize(0);
                    entry.setCompressedSize(0);
                    entry.setCrc(0);
                    out.putNextEntry(entry);
                    out.closeEntry();
                }

                void addFileEntry(String name, File input) throws IOException {
                    final ZipEntry entry = entry(name);
                    if (ZIP_EXTENSIONS.matcher(name).matches()) {
                        final long length = input.length();
                        entry.setMethod(ZipOutputStream.STORED);
                        entry.setSize(length);
                        entry.setCompressedSize(length);
                        entry.setCrc(crc32(input));
                    }
                    Copy.copy(new FileStore(input), zipEntrySink(entry));
                }

                long crc32(File input) throws IOException {
                    final Checksum checksum = new CRC32();
                    try (InputStream in = new CheckedInputStream(
                            new BufferedInputStream(
                                new FileInputStream(input)), checksum)) {
                        while (-1 != in.read()) {
                        }
                    }
                    return checksum.getValue();
                }

                Sink zipEntrySink(ZipEntry entry) {
                    return new ZipEntrySink(entry, out);
                }

                ZipEntry entry(String name) { return new ZipEntry(name); }
            } // Jar

            new Jar().jar(directory, "");
        }
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

    private interface FileTask {
        void process(File file) throws Exception;
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

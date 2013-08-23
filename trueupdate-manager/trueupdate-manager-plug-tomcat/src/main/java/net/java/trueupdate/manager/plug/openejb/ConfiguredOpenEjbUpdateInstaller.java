/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.plug.openejb;

import java.io.*;
import java.net.URI;
import java.util.*;
import java.util.logging.*;
import java.util.regex.Pattern;
import java.util.zip.*;
import javax.annotation.concurrent.Immutable;
import net.java.trueupdate.artifact.spec.*;
import net.java.trueupdate.core.io.*;
import net.java.trueupdate.core.util.EntrySink;
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
        final File directory = resolveInstallationDirectory();
        logger.log(Level.FINE, "Resolved current location {0} to installation directory {1} .",
                new Object[] { currentLocation(), directory });
        final File patch = resolver.resolveZipPatchFile(updateDescriptor());
        logger.log(Level.FINER, "Resolved update descriptor {0} to ZIP patch file {1} .",
                new Object[] { updateDescriptor(), patch });

        class PatchTask implements FileTask {
            @Override public void process(final File input) throws IOException {
                // TODO...
            }
        } // PatchTask

        withZipFile(directory, new PatchTask());
    }

    private File resolveInstallationDirectory() throws FileNotFoundException {
        final URI location = currentLocation();
        final Scheme scheme = Scheme.valueOf(location.getScheme());
        for (final AppInfo info : deployer.getDeployedApps())
            if (scheme.matches(location, info))
                return new File(info.path);
        throw new FileNotFoundException(
                String.format("Cannot locate installation directory of %s .", location));
    }

    private interface FileTask {
        void process(File file) throws IOException;
    }

    private void withZipFile(final File directory, final FileTask task)
    throws Exception {
        final File temp = File.createTempFile("input", ".zip");
        try {
            logger.log(Level.FINEST, "Created temporary file {0} .", temp);
            zipUpTo(directory, temp);
            logger.log(Level.FINER, "Zipped up {0} to {1} .",
                    new Object[] { directory, temp });
            task.process(temp);
        } finally {
            if (!temp.delete())
                throw new IOException(String.format(
                        "Cannot delete temporary file %s .", temp));
            logger.log(Level.FINEST, "Deleted temporary file {0} .", temp);
        }
    }

    private static void zipUpTo(final File directory, final File zipFile)
    throws IOException {
        assert directory.isDirectory();
        try (ZipOutputStream
                out = new ZipOutputStream(new FileOutputStream(zipFile))) {

            class Zipper  {
                void zipDirectory(final File directory, final String name)
                throws IOException {
                    final File[] memberFiles = directory.listFiles();
                    Arrays.sort(memberFiles); // courtesy
                    for (final File memberFile : memberFiles) {
                        final String memberName =
                                (name.isEmpty() ? name : name + '/')
                                + memberFile.getName();
                        if (memberFile.isDirectory()) {
                            addDirectoryEntry(memberName);
                            zipDirectory(memberFile, memberName);
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
                    Copy.copy(new FileStore(input), entrySink(entry));
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

                Sink entrySink(ZipEntry entry) {
                    return new EntrySink(entry, out);
                }

                ZipEntry entry(String name) { return new ZipEntry(name); }
            } // ZipUp

            new Zipper().zipDirectory(directory, "");
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

    private enum Scheme {
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
}

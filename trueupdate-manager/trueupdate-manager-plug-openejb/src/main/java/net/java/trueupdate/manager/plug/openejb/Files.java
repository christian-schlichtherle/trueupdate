/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.plug.openejb;

import java.io.*;
import java.util.*;
import java.util.jar.*;
import java.util.regex.Pattern;
import java.util.zip.*;
import javax.annotation.CheckForNull;
import net.java.trueupdate.core.io.*;
import net.java.trueupdate.core.zip.patch.ZipPatch;

/**
 * Provides file functions.
 *
 * @author Christian Schlichtherle
 */
final class Files {

    private static final Pattern COMPRESSED_FILE_EXTENSIONS = Pattern.compile(
            ".*\\.(ear|jar|war|zip|gz|xz)", Pattern.CASE_INSENSITIVE);

    private Files() { }

    /**
     * Creates an empty slot in the file system for a sibling of the given file
     * in the same directory.
     * Upon return, the slot can be subsequently created as a file or directory.
     *
     * @param sibling the file the caller wishes to have a sibling for in the
     *        same directory.
     * @return the empty slot for the file or directory to be subsequently
     *         created by the caller.
     */
    public static File createTempSlotForSibling(final File sibling)
    throws IOException {
        final File temp = File.createTempFile("temp", null, sibling.getParentFile());
        if (!temp.delete())
            throw new IOException(String.format("Cannot delete temporary file %s .", temp));
        return temp;
    }

    /**
     * Deletes the given file or directory with all members.
     *
     * @param file the file or directory to delete.
     * @return {@code false} if and only if a file or directory exists, but
     *         cannot get deleted, e.g. because of insufficient access
     *         permissions.
     */
    public static boolean deleteAll(final File file)
    throws IOException {
        if (file.isDirectory())
            for (File member : file.listFiles())
                if (!deleteAll(member))
                    return false;
        return file.delete() || !file.exists();
    }

    public static void jarTo(final File fileOrDirectory, final File jarFile)
    throws IOException {
        try (JarOutputStream
                out = new JarOutputStream(new FileOutputStream(jarFile))) {

            class Jar  {
                void jarDirectory(final File directory, final String name)
                throws IOException {
                    final File[] memberFiles = directory.listFiles();
                    Arrays.sort(memberFiles); // courtesy
                    for (final File memberFile : memberFiles) {
                        final String memberName =
                                (name.isEmpty() ? name : name + '/')
                                + memberFile.getName();
                        if (memberFile.isDirectory()) {
                            jarDirectory(memberFile, memberName);
                        } else {
                            jarFile(memberFile, memberName);
                        }
                    }
                }

                void jarFile(File file, String name) throws IOException {
                    final ZipEntry entry = entry(name);
                    if (COMPRESSED_FILE_EXTENSIONS.matcher(name).matches()) {
                        final long length = file.length();
                        entry.setMethod(ZipOutputStream.STORED);
                        entry.setSize(length);
                        entry.setCompressedSize(length);
                        entry.setCrc(crc32(file));
                    }
                    Copy.copy(fileSource(file), zipEntrySink(entry));
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

                Source fileSource(File file) {
                    return new FileStore(file);
                }

                Sink zipEntrySink(ZipEntry entry) {
                    return new ZipEntrySink(entry, out);
                }

                ZipEntry entry(String name) { return new ZipEntry(name); }
            } // Jar

            if (fileOrDirectory.isDirectory())
                new Jar().jarDirectory(fileOrDirectory, "");
            else
                new Jar().jarFile(fileOrDirectory, "");
        }
    }

    public static void unjarTo(final File jarFile, final File directory)
    throws IOException {
        try (JarFile jar = new JarFile(jarFile)) {

            for (final Enumeration<JarEntry> en = jar.entries();
                    en.hasMoreElements(); ) {
                final JarEntry entry = en.nextElement();
                if (entry.isDirectory()) continue;
                final File file = new File(directory, entry.getName());
                file.getParentFile().mkdirs();
                Copy.copy(new ZipEntrySource(entry, jar), new FileStore(file));
            }
        }
    }

    public static void applyPatchTo(
            final File originalJarFile,
            final File zipPatchFile,
            final File updatedJarFile)
    throws IOException {
        try (JarFile originalJar = new JarFile(originalJarFile, false);
             ZipFile zipPatch = new ZipFile(zipPatchFile)) {
            ZipPatch.builder()
                    .inputFile(originalJar)
                    .patchFile(zipPatch)
                    .createJarFile(true)
                    .build()
                    .applyZipPatchFileTo(new FileStore(updatedJarFile));
        }
    }

    public static void loanTempFile(
            final FileTask task,
            final String prefix,
            final @CheckForNull String suffix)
    throws Exception {
        final File temp = File.createTempFile(prefix, suffix);
        Exception ex = null;
        try {
            task.process(temp);
        } catch (Exception ex2) {
            throw ex = ex2;
        } finally {
            if (!temp.delete()) {
                final Exception ex2 = new IOException(String.format(
                        "Cannot delete temporary file %s .", temp));
                if (null != ex) {
                    ex.addSuppressed(ex2);
                    throw ex;
                }
                throw ex2;
            }
        }
    }

    @SuppressWarnings("PackageVisibleInnerClass")
    public interface FileTask {
        void process(File file) throws Exception;
    }
}

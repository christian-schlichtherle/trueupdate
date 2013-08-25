/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.plug.openejb;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.regex.Pattern;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;
import java.util.zip.Checksum;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import javax.annotation.CheckForNull;
import net.java.trueupdate.core.io.Copy;
import net.java.trueupdate.core.io.FileStore;
import net.java.trueupdate.core.io.Sink;
import net.java.trueupdate.core.io.ZipEntrySink;
import net.java.trueupdate.core.io.ZipEntrySource;
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

    public static void jarTo(final File directory, final File jarFile)
    throws IOException {
        if (!directory.isDirectory()) throw new IllegalArgumentException();

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
                    if (COMPRESSED_FILE_EXTENSIONS.matcher(name).matches()) {
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

    public static void unjarTo(final File jarFile, final File directory)
    throws IOException {
        if (!jarFile.isFile()) throw new IllegalArgumentException();

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

    public interface FileTask {
        void process(File file) throws Exception;
    }
}

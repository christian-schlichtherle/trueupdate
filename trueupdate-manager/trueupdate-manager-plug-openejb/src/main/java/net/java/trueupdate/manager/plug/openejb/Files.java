/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.plug.openejb;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.zip.*;
import javax.annotation.CheckForNull;
import net.java.trueupdate.core.io.*;
import net.java.trueupdate.core.zip.*;
import net.java.trueupdate.core.zip.patch.ZipPatch;

/**
 * Provides file functions.
 *
 * @author Christian Schlichtherle
 */
class Files {

    private static final Pattern COMPRESSED_FILE_EXTENSIONS = Pattern.compile(
            ".*\\.(ear|jar|war|zip|gz|xz)", Pattern.CASE_INSENSITIVE);

    private Files() { }

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

    public static void zipTo(final File fileOrDirectory, final File zipFile)
    throws IOException {

        class WithZipArchive implements ZipOutputTask<Void, IOException> {

            @Override public Void execute(final ZipOutput output)
            throws IOException {

                class Zipper {
                    void zipDirectory(final File directory, final String name)
                    throws IOException {
                        final File[] memberFiles = directory.listFiles();
                        Arrays.sort(memberFiles); // courtesy
                        for (final File memberFile : memberFiles) {
                            final String memberName =
                                    (name.isEmpty() ? name : name + '/')
                                    + memberFile.getName();
                            if (memberFile.isDirectory()) {
                                zipDirectory(memberFile, memberName);
                            } else {
                                zipFile(memberFile, memberName);
                            }
                        }
                    }

                    void zipFile(File file, String name) throws IOException {
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

                    long crc32(final File input) throws IOException {

                        class ReadTask implements InputTask<Long, IOException> {
                            @Override public Long execute(final InputStream in)
                                    throws IOException {
                                final Checksum checksum = new CRC32();
                                final InputStream cin =
                                        new CheckedInputStream(in, checksum);
                                final byte[] buf = new byte[Store.BUFSIZE];
                                while (-1 != cin.read(buf)) { }
                                return checksum.getValue();
                            }
                        } // ReadTask

                        return Sources.execute(new ReadTask()).on(input);
                    }

                    Source fileSource(File file) {
                        return new FileStore(file);
                    }

                    Sink zipEntrySink(ZipEntry entry) {
                        return new ZipEntrySink(entry, output);
                    }

                    ZipEntry entry(String name) { return new ZipEntry(name); }
                } // Zipper

                if (fileOrDirectory.isDirectory())
                    new Zipper().zipDirectory(fileOrDirectory, "");
                else
                    new Zipper().zipFile(fileOrDirectory, "");
                return null;
            }
        } // WithZipArchive

        ZipSinks.execute(new WithZipArchive()).on(zipFile);
    }

    public static void unzipTo(final File zipFile, final File directory)
    throws IOException {

        class OnArchiveTask implements ZipInputTask<Void, IOException> {

            @Override
            public Void execute(final ZipInput input) throws IOException {
                for (final ZipEntry entry : input) {
                    if (entry.isDirectory()) continue;
                    final File file = new File(directory, entry.getName());
                    file.getParentFile().mkdirs();
                    Copy.copy(new ZipEntrySource(entry, input),
                              new FileStore(file));
                }
                return null;
            }
        } // OnArchiveTask

        ZipSources.execute(new OnArchiveTask()).on(zipFile);
    }

    public static void applyPatchTo(
            File inputFile,
            File patchFile,
            File patchedFile)
    throws IOException {
        ZipPatch.builder()
                .input(inputFile)
                .diff(patchFile)
                .build()
                .output(patchedFile);
    }

    /**
     * Creates an temporary file and deletes it immediately in order to create
     * an empty slot in the file system.
     * Upon return, the slot can be subsequently created as a file or directory.
     *
     * @return the empty slot for the file or directory to be subsequently
     *         created by the caller.
     */
    public static File createEmptySlot(
            final String prefix,
            final @CheckForNull String suffix)
    throws IOException {
        final File temp = File.createTempFile(prefix, suffix);
        if (!temp.delete())
            throw new IOException(String.format("Cannot delete temporary file %s .", temp));
        return temp;
    }

    public static void loanTempFile(
            final FileTask task,
            final String prefix,
            final @CheckForNull String suffix)
    throws Exception {
        final File temp = File.createTempFile(prefix, suffix);
        Exception ex = null;
        try {
            task.execute(temp);
        } catch (Exception ex2) {
            throw ex = ex2;
        } finally {
            if (!temp.delete() && null == ex)
                throw new IOException(String.format(
                    "Cannot delete temporary file %s .", temp));
        }
    }

    @SuppressWarnings("PackageVisibleInnerClass")
    public interface FileTask {
        void execute(File file) throws Exception;
    }
}

/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.core.io;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.zip.*;
import javax.annotation.CheckForNull;
import net.java.trueupdate.core.io.*;
import net.java.trueupdate.core.zip.patch.ZipPatch;

/**
 * Provides functions for {@link File}s.
 *
 * @author Christian Schlichtherle
 */
public final class Files {

    private static final Pattern COMPRESSED_FILE_EXTENSIONS = Pattern.compile(
            ".*\\.(ear|jar|war|zip|gz|xz)", Pattern.CASE_INSENSITIVE);

    public static void zip(File zipFile, File fileOrDirectory)
    throws IOException {
        zip(zipFile, fileOrDirectory,
            fileOrDirectory.isDirectory() ? "" : fileOrDirectory.getName());
    }

    public static void zip(final File zipFile,
                           final File fileOrDirectory,
                           final String entryName)
    throws IOException {

        class WithZipArchive implements ZipOutputTask<Void, IOException> {

            @Override public Void execute(final ZipOutputStream zipArchive)
            throws IOException {

                class Zipper {
                    void zipDirectory(final File directory,
                                      final String entryName)
                    throws IOException {
                        final File[] memberFiles = directory.listFiles();
                        Arrays.sort(memberFiles); // courtesy
                        for (final File memberFile : memberFiles) {
                            final String memberName =
                                    (entryName.isEmpty() ? entryName : entryName + '/')
                                    + memberFile.getName();
                            if (memberFile.isDirectory()) {
                                zipDirectory(memberFile, memberName);
                            } else {
                                zipFile(memberFile, memberName);
                            }
                        }
                    }

                    void zipFile(final File file, final String entryName)
                    throws IOException {
                        final ZipEntry entry = entry(entryName);
                        if (COMPRESSED_FILE_EXTENSIONS.matcher(entryName).matches()) {
                            final long length = file.length();
                            entry.setMethod(ZipOutputStream.STORED);
                            entry.setSize(length);
                            entry.setCompressedSize(length);
                            entry.setCrc(crc32(file));
                        }
                        Copy.copy(fileSource(file), zipEntrySink(entry));
                    }

                    long crc32(final File input) throws IOException {
                        final Checksum checksum = new CRC32();

                        class ReadTask implements InputTask<Void, IOException> {
                            @Override
                            public Void execute(InputStream in) throws IOException {
                                while (-1 != in.read()) {
                                }
                                return null;
                            }
                        } // ReadTask

                        Sources.execute(new ReadTask())
                                .on(new CheckedInputStream(
                                    new BufferedInputStream(
                                        new FileInputStream(input)),
                                    checksum));
                        return checksum.getValue();
                    }

                    Source fileSource(File file) {
                        return new FileStore(file);
                    }

                    Sink zipEntrySink(ZipEntry entry) {
                        return new ZipEntrySink(entry, zipArchive);
                    }

                    ZipEntry entry(String entryName) {
                        return new ZipEntry(entryName);
                    }
                } // Zipper

                if (fileOrDirectory.isDirectory())
                    new Zipper().zipDirectory(fileOrDirectory, entryName);
                else
                    new Zipper().zipFile(fileOrDirectory, entryName);
                return null;
            }
        } // WithZipArchive

        ZipSinks.execute(new WithZipArchive())
                .on(new ZipOutputStream(new FileOutputStream(zipFile)));
    }

    public static void unzip(final File zipFile, final File directory)
    throws IOException {

        class OnArchiveTask implements ZipInputTask<Void, IOException> {

            @Override
            public Void execute(final ZipFile archive) throws IOException {
                for (final Enumeration<? extends ZipEntry>
                             en = archive.entries();
                        en.hasMoreElements(); ) {
                    final ZipEntry entry = en.nextElement();
                    if (entry.isDirectory()) continue;
                    final File file = new File(directory, entry.getName());
                    file.getParentFile().mkdirs();
                    Copy.copy(new ZipEntrySource(entry, archive),
                              new FileStore(file));
                }
                return null;
            }
        } // OnArchiveTask

        ZipSources.execute(new OnArchiveTask()).on(new ZipFile(zipFile));
    }

    public static void patchZip(
            final File input,
            final File diff,
            final File output,
            final boolean createJar)
    throws IOException {

        class OnInputArchiveTask implements ZipInputTask<Void, IOException> {

            @Override
            public Void execute(final ZipFile input) throws IOException {

                class OnPatchArchiveTask implements ZipInputTask<Void, IOException> {

                    @Override
                    public Void execute(final ZipFile diff) throws IOException {
                        ZipPatch.builder()
                                .input(input)
                                .diff(diff)
                                .createJar(createJar)
                                .build()
                                .outputTo(new FileStore(output));
                        return null;
                    }
                } // OnPatchArchiveTask

                ZipSources.execute(new OnPatchArchiveTask())
                          .on(new ZipFile(diff));
                return null;
            }
        } // OnInputArchiveTask

        ZipSources.execute(new OnInputArchiveTask())
                  .on(new ZipFile(input));
    }

    /**
     * Forwards the call to
     * {@link #createTempPath(String, String) createTempPath(prefix, null)}.
     */
    public static File createTempPath(final String prefix) throws IOException {
        return createTempPath(prefix, null);
    }

    /**
     * Forwards the call to
     * {@link #createTempPath(String, String, File)
     *        createTempPath(prefix, suffix, null)}.
     */
    public static File createTempPath(
            final String prefix,
            final @CheckForNull String suffix)
    throws IOException {
        return createTempPath(prefix, suffix, null);
    }

    /**
     * Creates a temporary file in the given directory and deletes it
     * immediately in order to create a temporary path for an entity in the
     * file system.
     * Upon return, the entity can get created using the path of the returned
     * {@code File} object.
     *
     * @return the temporary path for the entity to be subsequently created by
     *         the caller.
     */
    public static File createTempPath(
            final String prefix,
            final @CheckForNull String suffix,
            final @CheckForNull File directory)
    throws IOException {
        final File temp = File.createTempFile(prefix, suffix, directory);
        deletePath(temp);
        return temp;
    }

    public static <V, X extends Exception> V loanTempFile(
            final FileTask<V, X> task,
            final String prefix)
    throws X, IOException {
        return loanTempFile(task, prefix, null);
    }

    public static <V, X extends Exception> V loanTempFile(
            final FileTask<V, X> task,
            final String prefix,
            final @CheckForNull String suffix)
    throws X, IOException {
        return loanTempFile(task, prefix, suffix, null);
    }

    @SuppressWarnings("unchecked")
    public static <V, X extends Exception> V loanTempFile(
            final FileTask<V, X> task,
            final String prefix,
            final @CheckForNull String suffix,
            final @CheckForNull File directory)
    throws X, IOException {
        final File temp = File.createTempFile(prefix, suffix, directory);
        X ex = null;
        try {
            return task.execute(temp);
        } catch (Exception ex2) {
            throw ex = (X) ex2;
        } finally {
            try {
                deletePath(temp);
            } catch (IOException ex2) {
                if (null == ex) throw ex2;
            }
        }
    }

    public static void copyFile(final File from, final File to)
    throws IOException {
        try {
            Copy.copy(new FileStore(from), new FileStore(to));
        } catch (IOException ex) {
            throw new IOException(String.format("Cannot copy %s to %s .",
                    from, to), ex);
        }
    }

    public static void renamePath(File from, File to) throws IOException {
        if (!from.renameTo(to))
            throw new IOException(String.format("Cannot rename %s to %s .",
                    from, to));
    }

    /**
     * Deletes the given file or directory with all members.
     *
     * @param file the file or directory to delete.
     * @throws IOException if and only if a file or directory exists, but
     *         cannot get deleted, e.g. because of insufficient access
     *         permissions.
     */
    public static void deletePath(final @CheckForNull File file)
    throws IOException {
        if (null == file || file.delete()) return;
        if (file.isDirectory())
            for (File member : file.listFiles())
                deletePath(member);
        if (!file.delete() && file.exists())
            throw new IOException(String.format("Cannot delete %s .", file));
    }

    private Files() { }
}

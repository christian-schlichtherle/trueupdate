/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.installer.core.io;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import javax.annotation.CheckForNull;
import net.java.trueupdate.core.io.Copy;
import net.java.trueupdate.core.io.FileStore;
import net.java.trueupdate.core.io.Sink;
import net.java.trueupdate.core.io.Source;
import net.java.trueupdate.core.zip.io.ZipEntrySink;
import net.java.trueupdate.core.zip.io.ZipEntrySource;
import net.java.trueupdate.core.zip.io.ZipFileStore;
import net.java.trueupdate.core.zip.io.ZipInput;
import net.java.trueupdate.core.zip.io.ZipInputTask;
import net.java.trueupdate.core.zip.io.ZipOutput;
import net.java.trueupdate.core.zip.io.ZipOutputTask;
import net.java.trueupdate.core.zip.io.ZipSink;
import net.java.trueupdate.core.zip.io.ZipSinks;
import net.java.trueupdate.core.zip.io.ZipSource;
import net.java.trueupdate.core.zip.io.ZipSources;
import net.java.trueupdate.installer.core.cmd.PathTask;

/**
 * Provides I/O functions for {@link File}s.
 *
 * @author Christian Schlichtherle
 */
public final class Files {

    public static void zip(File zipFile,
                           File fileOrDirectory,
                           String entryName)
    throws IOException {
        zip(new ZipFileStore(zipFile), fileOrDirectory, entryName);
    }

    public static void zip(final ZipSink sink,
                           final File fileOrDirectory,
                           final String entryName)
    throws IOException {

        class ZipTask implements ZipOutputTask<Void, IOException> {
            @Override
            public Void execute(final ZipOutput output) throws IOException {

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

                    void zipFile(final File file, final String name)
                    throws IOException {
                        Copy.copy(source(file), sink(entry(name)));
                    }

                    Source source(File file) { return new FileStore(file); }

                    Sink sink(ZipEntry entry) {
                        return new ZipEntrySink(entry, output);
                    }

                    ZipEntry entry(String name) { return output.entry(name); }
                } // Zipper

                if (fileOrDirectory.isDirectory())
                    new Zipper().zipDirectory(fileOrDirectory, entryName);
                else
                    new Zipper().zipFile(fileOrDirectory, entryName);
                return null;
            }
        } // ZipTask

        ZipSinks.execute(new ZipTask()).on(sink);
    }

    public static void unzip(File zipFile, File directory)
    throws IOException {
        unzip(new ZipFileStore(zipFile), directory);
    }

    public static void unzip(final ZipSource source, final File directory)
    throws IOException {

        class UnzipTask implements ZipInputTask<Void, IOException> {
            @Override public Void execute(final ZipInput input) throws IOException {
                for (final ZipEntry entry : input) {
                    if (entry.isDirectory()) continue;
                    final File file = new File(directory, entry.getName());
                    file.getParentFile().mkdirs();
                    Copy.copy(new ZipEntrySource(entry, input),
                              new FileStore(file));
                }
                return null;
            }
        } // UnzipTask

        ZipSources.execute(new UnzipTask()).on(source);
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

    public static <V> V loanTempDir(
            final PathTask<V, Exception> task,
            final String prefix,
            final @CheckForNull String suffix,
            final @CheckForNull File directory)
    throws Exception {
        class DeleteAndForwardTask implements PathTask<V, Exception> {
            @Override public V execute(final File file) throws Exception {
                deletePath(file);
                if (!file.mkdir())
                    throw new IOException(String.format(
                            "Could not create temporary directory '%s'.", file));
                return task.execute(file);
            }
        }
        return loanTempFile(new DeleteAndForwardTask(), prefix, suffix, directory);
    }

    @SuppressWarnings("unchecked")
    public static <V, X extends Exception> V loanTempFile(
            final PathTask<V, X> task,
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
            throw new IOException(String.format("Could not copy '%s' to '%s'.",
                    from, to), ex);
        }
    }

    public static void renamePath(File from, File to) throws IOException {
        if (!from.renameTo(to))
            throw new IOException(String.format("Could not rename '%s' to '%s'.",
                    from, to));
    }

    /**
     * Deletes the given file or directory with all members.
     *
     * @param file the file or directory to delete.
     * @throws IOException if and only if a file or directory exists, but
     *         could not get deleted, e.g. because of insufficient access
     *         permissions.
     */
    public static void deletePath(final File file) throws IOException {
        if (file.delete()) return;
        if (file.isDirectory()) {
            final File[] members = file.listFiles();
            if (null != members)
                for (File member : members)
                    deletePath(member);
        }
        if (!file.delete() && file.exists())
            throw new IOException(String.format("Could not delete '%s'.", file));
    }

    private Files() { }
}

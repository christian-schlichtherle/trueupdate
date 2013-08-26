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
import javax.annotation.WillClose;
import net.java.trueupdate.core.io.*;
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

        class JarFileSink implements ZipSink {
            @Override public ZipOutputStream output() throws IOException {
                return new JarOutputStream(new FileOutputStream(jarFile));
            }
        } // JarOutputStreamSink

        new ZipOutputTask<Void, IOException>(new JarFileSink()) {
            @Override protected Void execute(final ZipOutputStream zipOut)
            throws IOException {

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

                    long crc32(final File input) throws IOException {
                        final Checksum checksum = new CRC32();

                        class CheckedInputStreamSource implements Source {
                            @Override
                            public InputStream input() throws IOException {
                                return new CheckedInputStream(
                                new BufferedInputStream(
                                    new FileInputStream(input)), checksum);
                            }
                        } // CheckedInputStreamSource

                        new InputTask<Void, IOException>(new CheckedInputStreamSource()) {
                            @Override
                            protected Void execute(InputStream in) throws IOException {
                                while (-1 != in.read()) {
                                }
                                return null;
                            }
                        }.call();
                        return checksum.getValue();
                    }

                    Source fileSource(File file) {
                        return new FileStore(file);
                    }

                    Sink zipEntrySink(ZipEntry entry) {
                        return new ZipEntrySink(entry, zipOut);
                    }

                    ZipEntry entry(String name) { return new ZipEntry(name); }
                } // Jar

                if (fileOrDirectory.isDirectory())
                    new Jar().jarDirectory(fileOrDirectory, "");
                else
                    new Jar().jarFile(fileOrDirectory, "");
                return null;
            }
        }.call();
    }

    public static void unjarTo(final File jarFile, final File directory)
    throws Exception {

        class WithJarFile implements ZipFileTask {

            @Override
            public void execute(final ZipFile jar) throws Exception {
                for (final Enumeration<JarEntry> en = ((JarFile) jar).entries();
                        en.hasMoreElements(); ) {
                    final JarEntry entry = en.nextElement();
                    if (entry.isDirectory()) continue;
                    final File file = new File(directory, entry.getName());
                    file.getParentFile().mkdirs();
                    Copy.copy(new ZipEntrySource(entry, jar), new FileStore(file));
                }
            }

        } // WithJarFile

        loanZipFileTo(new JarFile(jarFile), new WithJarFile());
    }

    public static void applyPatchTo(
            final File originalJarFile,
            final File zipPatchFile,
            final File updatedJarFile)
    throws Exception {

        class WithOriginalJarFile implements ZipFileTask {

            @Override
            public void execute(final ZipFile originalJarFile) throws Exception {

                class WithZipPatchFile implements ZipFileTask {

                    @Override
                    public void execute(final ZipFile zipPatchFile) throws Exception {
                        ZipPatch.builder()
                                .inputFile(originalJarFile)
                                .patchFile(zipPatchFile)
                                .createJarFile(true)
                                .build()
                                .applyZipPatchFileTo(new FileStore(updatedJarFile));
                    }

                } // WithZipPatchFile

                loanZipFileTo(new ZipFile(zipPatchFile), new WithZipPatchFile());
            }
        } // WithOriginalJarFile

        loanZipFileTo(new JarFile(originalJarFile, false), new WithOriginalJarFile());
    }

    public static void loanZipFileTo(
            final @WillClose ZipFile zipFile,
            final ZipFileTask task)
    throws Exception {

        class ZipFileSource implements ZipSource {
            @Override public ZipFile input() throws IOException {
                return zipFile;
            }
        } // ZipFileSource

        new ZipInputTask<Void, Exception>(new ZipFileSource()) {
            @Override protected Void execute(ZipFile zipFile) throws Exception {
                task.execute(zipFile);
                return null;
            }
        }.call();
    }

    @SuppressWarnings("PackageVisibleInnerClass")
    public interface ZipFileTask {
        void execute(ZipFile zipFile) throws Exception;
    }

    public static void loanTempFileTo(
            final String prefix,
            final @CheckForNull String suffix,
            final FileTask task)
    throws Exception {
        final File temp = File.createTempFile(prefix, suffix);
        Exception ex = null;
        try {
            task.process(temp);
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
        void process(File file) throws Exception;
    }
}

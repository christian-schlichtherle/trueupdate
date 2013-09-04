/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.core.zip.patch;

import java.io.IOException;
import java.io.OutputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Applies a ZIP patch file to an input ZIP file and writes an output JAR file.
 *
 * @author Christian Schlichtherle
 */
public abstract class RawJarPatch extends RawZipPatch {

    @Override
    ZipOutputStream newZipOutputStream(OutputStream out) throws IOException {
        return new JarOutputStream(out);
    }

    @Override ZipEntry newZipEntry(String name) { return new JarEntry(name); }

    @Override EntryNameFilter[] passFilters() {
        // The JarInputStream class assumes that the file entry
        // "META-INF/MANIFEST.MF" should either be the first or the second
        // entry (if preceded by the directory entry "META-INF/"), so we need
        // to process the ZIP patch file in two passes with a corresponding
        // filter to ensure this order.
        // Note that the directory entry "META-INF/" is always part of the
        // unchanged patch set because it's content is always empty.
        // Thus, by copying the unchanged entries before the changed entries,
        // the directory entry "META-INF/" will always appear before the file
        // entry "META-INF/MANIFEST.MF".
        final EntryNameFilter manifestFilter = new ManifestEntryNameFilter();
        return new EntryNameFilter[] {
                manifestFilter,
                new InverseEntryNameFilter(manifestFilter)
        };
    }
}

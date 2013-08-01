/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.core.zip.patch;

import com.stimulus.archiva.update.core.io.Sink;
import java.io.IOException;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Applies a ZIP patch file to an input ZIP or JAR file and writes an output
 * JAR file.
 *
 * @author Christian Schlichtherle
 */
abstract class JarPatch extends ZipPatch {

    @Override
    ZipOutputStream newZipOutputStream(Sink outputZipFile) throws IOException {
        return new JarOutputStream(outputZipFile.output());
    }

    @Override ZipEntry newZipEntry(String name) { return new JarEntry(name); }

    @Override Filter[] passFilters() {
        // The JarInputStream class assumes that the file entry
        // "META-INF/MANIFEST.MF" should be either the first or the second
        // entry (if preceded by the directory entry "META-INF/"), so we need
        // to process the ZIP patch file in two passes with a corresponding
        // filter to ensure this order.
        // Note that the directory entry "META-INF/" is always part of the
        // unchanged patch set because it's content is always empty.
        // Thus, by copying the unchanged entries before the changed entries,
        // the directory entry "META-INF/" will always appear before the file
        // entry "META-INF/MANIFEST.MF".
        final Filter manifestFilter = new ManifestFilter();
        return new Filter[] {
                manifestFilter,
                new InverseFilter(manifestFilter)
        };
    }
}

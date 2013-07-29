/*
 * Copyright (C) 2005-2013 Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.server.jar.patch;

import com.stimulus.archiva.update.core.codec.JaxbCodec;
import com.stimulus.archiva.update.core.io.*;
import com.stimulus.archiva.update.server.jar.commons.EntrySource;
import com.stimulus.archiva.update.server.jar.model.*;
import java.io.*;
import static java.util.Objects.requireNonNull;

import java.util.Enumeration;
import java.util.jar.*;
import java.util.zip.*;
import javax.annotation.WillNotClose;
import javax.annotation.concurrent.Immutable;
import javax.xml.bind.*;

/**
 * Applies a JAR diff file to an input JAR file.
 *
 * @author Christian Schlichtherle
 */
@Immutable
public abstract class JarPatch {

    private volatile Diff diff;

    /** Returns the JAR diff file. */
    abstract @WillNotClose ZipFile jarDiffFile();

    /** Returns the input JAR file. */
    abstract @WillNotClose JarFile inputJarFile();

    /** Returns the JAXB context for unmarshalling the JAR diff bean. */
    abstract JAXBContext jaxbContext();

    /**
     * Applies the configured JAR diff file.
     *
     * @param outputJarFile the sink for writing the output JAR file.
     */
    public final void applyDiffFileTo(final Sink outputJarFile)
    throws IOException {
        try (JarOutputStream out = new JarOutputStream(outputJarFile.output())) {
            // The JarInputStream class requires that the entry with the name
            // "META-INF/MANIFEST.MF" and the optional entry with the name
            // "META-INF/" are the first two entries, so we need to process the
            // JAR diff file in two passes with a corresponding filter.
            final Filter manifestFilter = new ManifestFilter();
            applyDiffFileTo(manifestFilter, out);
            applyDiffFileTo(new InverseFilter(manifestFilter), out);
        }
    }

    private void applyDiffFileTo(
            final Filter filter,
            final @WillNotClose JarOutputStream out)
    throws IOException {

        class EntrySink implements Sink {

            final String name;

            EntrySink(final String name) { this.name = name; }

            @Override public OutputStream output() throws IOException {
                out.putNextEntry(new JarEntry(name));
                return new FilterOutputStream(out) {
                    @Override public void close() throws IOException {
                        ((JarOutputStream) out).closeEntry();
                    }
                };
            }
        } // EntrySink

        class OutputJarFileWriter {

            OutputJarFileWriter writeUnchanged() throws IOException {
                for (final Enumeration<JarEntry>
                             entries = inputJarFile().entries();
                     entries.hasMoreElements(); ) {
                    final JarEntry entry = entries.nextElement();
                    final String name = entry.getName();
                    if (unchanged(name))
                        copyIfAcceptedByFilter(
                                new EntrySource(entry, inputJarFile()),
                                new EntrySink(name));
                }
                return this;
            }

            boolean unchanged(String name) throws IOException {
                return null != diff().unchanged(name);
            }

            OutputJarFileWriter writeAddedOrChanged() throws IOException {
                for (final Enumeration<? extends ZipEntry>
                             entries = jarDiffFile().entries();
                     entries.hasMoreElements(); ) {
                    final ZipEntry entry = entries.nextElement();
                    final String name = entry.getName();
                    if (addedOrChanged(name))
                        copyIfAcceptedByFilter(
                                new EntrySource(entry, jarDiffFile()),
                                new EntrySink(name));
                }
                return this;
            }

            boolean addedOrChanged(String name) throws IOException {
                return null != diff().added(name) ||
                        null != diff().changed(name);
            }

            void copyIfAcceptedByFilter(EntrySource source, EntrySink sink)
            throws IOException {
                if (filter.accept(source)) Copy.copy(source, sink);
            }
        } // OutputJarFileWriter

        new OutputJarFileWriter().writeUnchanged().writeAddedOrChanged();
    }

    /** Returns the JAR diff bean. */
    private Diff diff() throws IOException {
        final Diff diff = this.diff;
        return null != diff ? diff : (this.diff = loadDiff());
    }

    private Diff loadDiff() throws IOException {
        final ZipEntry entry = jarDiffFile().getEntry(Diffs.DIFF_ENTRY_NAME);
        if (null == entry)
            throw new IOException(Diffs.DIFF_ENTRY_NAME + " (entry not found)");
        try {
            return new JaxbCodec(jaxbContext()).decode(
                    new EntrySource(entry, jarDiffFile()), Diff.class);
        } catch (IOException | RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IOException(ex);
        }
    }

    /**
     * A builder for a JAR patch.
     * The default JAXB context binds only the {@link Diff} class.
     */
    public static class Builder {

        private ZipFile jarDiffFile;
        private JarFile inputJarFile;
        private JAXBContext jaxbContext;

        public Builder jarDiffFile(final ZipFile jarDiffFile) {
            this.jarDiffFile = requireNonNull(jarDiffFile);
            return this;
        }

        public Builder inputJarFile(final JarFile inputJarFile) {
            this.inputJarFile = requireNonNull(inputJarFile);
            return this;
        }

        public Builder jaxbContext(final JAXBContext jaxbContext) {
            this.jaxbContext = requireNonNull(jaxbContext);
            return this;
        }

        public JarPatch build() {
            return build(jarDiffFile, inputJarFile,
                    null != jaxbContext ? jaxbContext : Diffs.jaxbContext());
        }

        private static JarPatch build(
                final ZipFile jarDiffFile,
                final JarFile inputJarFile,
                final JAXBContext jaxbContext) {
            requireNonNull(jarDiffFile);
            requireNonNull(inputJarFile);
            assert null != jaxbContext;
            return new JarPatch() {
                @Override ZipFile jarDiffFile() { return jarDiffFile; }
                @Override JarFile inputJarFile() { return inputJarFile; }
                @Override JAXBContext jaxbContext() { return jaxbContext; }
            };
        }
    } // Builder
}

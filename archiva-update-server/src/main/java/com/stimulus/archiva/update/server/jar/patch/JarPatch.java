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

    /** Returns the JAR diff file. */
    abstract @WillNotClose ZipFile diff();

    /** Returns the input JAR file. */
    abstract @WillNotClose JarFile input();

    /** Returns the JAXB context for unmarshalling the JAR {@link Diff}. */
    abstract JAXBContext jaxbContext();

    /**
     * Applies the configured JAR diff file.
     *
     * @param output the sink for writing the output JAR file.
     */
    public void applyDiffFileTo(final Sink output) throws IOException {
        try (JarOutputStream jarOut = new JarOutputStream(output.output())) {
            // The JarInputStream class requires that the entry with the name
            // "META-INF/MANIFEST.MF" and the optional entry with the name
            // "META-INF/" are the first two entries, so we need to process the
            // JAR diff file in two passes with a corresponding filter.
            final Filter manifestFilter = new ManifestFilter();
            applyDiffFileTo(jarOut, manifestFilter);
            applyDiffFileTo(jarOut, new InverseFilter(manifestFilter));
        }
    }

    public void applyDiffFileTo(
            final @WillNotClose JarOutputStream jarOut,
            final Filter filter)
    throws IOException {

        class EntrySink implements Sink {

            final String name;

            EntrySink(final String name) { this.name = name; }

            @Override public OutputStream output() throws IOException {
                jarOut.putNextEntry(new JarEntry(name));
                return new FilterOutputStream(jarOut) {
                    @Override public void close() throws IOException {
                        ((JarOutputStream) out).closeEntry();
                    }
                };
            }
        } // EntrySink

        class WithStreams {

            final Diff diff;

            WithStreams() throws IOException {
                final ZipEntry entry = diff().getEntry(Diffs.DIFF_ENTRY_NAME);
                if (null == entry)
                    throw new IOException("Missing " + Diffs.DIFF_ENTRY_NAME);
                try {
                    diff = new JaxbCodec(jaxbContext()).decode(
                            new EntrySource(entry, diff()), Diff.class);
                } catch (IOException | RuntimeException ex) {
                    throw ex;
                } catch (Exception ex) {
                    throw new IOException(ex);
                }
            }

            WithStreams copyUnchanged() throws IOException {
                for (final Enumeration<JarEntry> e = input().entries();
                     e.hasMoreElements(); ) {
                    final JarEntry entry = e.nextElement();
                    final String name = entry.getName();
                    if (unchanged(name)) {
                        final EntrySource
                                entrySource = new EntrySource(entry, input());
                        if (filter.accept(entrySource))
                            Copy.copy(entrySource, new EntrySink(name));
                    }
                }
                return this;
            }

            boolean unchanged(String name) {
                return null != diff.unchanged(name);
            }

            WithStreams copyAddedOrChanged() throws IOException {
                for (final Enumeration<? extends ZipEntry> e = diff().entries();
                     e.hasMoreElements(); ) {
                    final ZipEntry entry = e.nextElement();
                    final String name = entry.getName();
                    if (addedOrChanged(name)) {
                        final EntrySource
                                entrySource = new EntrySource(entry, diff());
                        if (filter.accept(entrySource))
                            Copy.copy(entrySource, new EntrySink(name));
                    }
                }
                return this;
            }

            boolean addedOrChanged(String name) {
                return null != diff.added(name) ||
                        null != diff.changed(name);
            }
        } // WithStreams

        new WithStreams().copyUnchanged().copyAddedOrChanged();
    }

    /**
     * A builder for a JAR patch.
     * The default JAXB context binds only the {@link Diff} class.
     */
    public static class Builder {

        private ZipFile diff;
        private JarFile input;
        private JAXBContext context;

        public Builder diff(final ZipFile diff) {
            this.diff = requireNonNull(diff);
            return this;
        }

        public Builder input(final JarFile input) {
            this.input = requireNonNull(input);
            return this;
        }

        public Builder jaxbContext(final JAXBContext context) {
            this.context = requireNonNull(context);
            return this;
        }

        public JarPatch build() {
            return build(diff, input,
                    null != context ? context : Diffs.jaxbContext());
        }

        private static JarPatch build(
                final ZipFile diff,
                final JarFile input,
                final JAXBContext context) {
            requireNonNull(diff);
            requireNonNull(input);
            assert null != context;
            return new JarPatch() {
                @Override ZipFile diff() { return diff; }
                @Override JarFile input() { return input; }
                @Override JAXBContext jaxbContext() { return context; }
            };
        }
    } // Builder
}

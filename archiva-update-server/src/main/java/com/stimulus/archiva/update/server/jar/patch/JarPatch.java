/*
 * Copyright (C) 2005-2013 Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.server.jar.patch;

import com.stimulus.archiva.update.core.codec.JaxbCodec;
import com.stimulus.archiva.update.core.io.*;
import com.stimulus.archiva.update.server.jar.model.*;

import java.io.*;

import static java.util.Objects.requireNonNull;
import java.util.jar.*;
import java.util.zip.*;
import javax.annotation.concurrent.Immutable;
import javax.xml.bind.*;

/**
 * Applies a JAR patch file to an input JAR file.
 *
 * @author Christian Schlichtherle
 */
@Immutable
public abstract class JarPatch {

    /** Returns the source for reading the JAR patch file. */
    abstract Source patch();

    /** Returns the source for reading the input JAR file. */
    abstract Source input();

    /** Returns the JAXB context for unmarshalling the JAR diff. */
    abstract JAXBContext jaxbContext();

    /**
     * Applies the configured JAR patch file.
     *
     * @param output the sink for writing the output JAR file.
     */
    public void applyPatchFileTo(final Sink output) throws IOException {
        try (ZipInputStream patchIn = new ZipInputStream(patch().input());
             ZipInputStream jarIn = new ZipInputStream(input().input());
             ZipOutputStream jarOut = new ZipOutputStream(output.output())) {

            class EntrySource implements Source {
                final ZipInputStream in;

                EntrySource(final ZipInputStream in) { this.in = in;}

                @Override public InputStream input() throws IOException {
                    return new FilterInputStream(in) {
                        @Override public void close() throws IOException {
                            ((ZipInputStream) in).closeEntry();
                        }
                    };
                }
            } // EntrySource

            class EntrySink implements Sink {
                final String name;

                EntrySink(final String name) { this.name = name; }

                @Override public OutputStream output() throws IOException {
                    jarOut.putNextEntry(new ZipEntry(name));
                    return new FilterOutputStream(jarOut) {
                        @Override public void close() throws IOException {
                            ((ZipOutputStream) out).closeEntry();
                        }
                    };
                }
            } // EntrySink

            class WithStreams {
                final Diff diff;

                WithStreams() throws IOException {
                    final ZipEntry entry = patchIn.getNextEntry();
                    if (!Diffs.DIFF_ENTRY_NAME.equals(entry.getName()))
                        throw new IOException("Expected " + Diffs.DIFF_ENTRY_NAME);
                    try {
                        diff = new JaxbCodec(jaxbContext()).decode(
                                new EntrySource(patchIn), Diff.class);
                    } catch (IOException | RuntimeException ex) {
                        throw ex;
                    } catch (Exception ex) {
                        throw new IOException(ex);
                    }
                }

                WithStreams copyUnchanged() throws IOException {
                    for (ZipEntry entry = null;
                         null != (entry = jarIn.getNextEntry()); ) {
                        final String name = entry.getName();
                        if (unchanged(name))
                            Copy.copy(new EntrySource(jarIn),
                                      new EntrySink(name));
                    }
                    return this;
                }

                boolean unchanged(String name) {
                    return null != diff.unchanged(name);
                }

                WithStreams copyAddedOrChanged() throws IOException {
                    for (ZipEntry entry = null;
                         null != (entry = patchIn.getNextEntry()); ) {
                        final String name = entry.getName();
                        assert addedOrChanged(name);
                        Copy.copy(new EntrySource(patchIn),
                                  new EntrySink(name));
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
    }

    /**
     * A builder for a JAR patch.
     * The default JAXB context binds only the {@link Diff} class.
     */
    public static class Builder {

        private Source patch, input;
        private JAXBContext context;

        public Builder patch(final Source patch) {
            this.patch = requireNonNull(patch);
            return this;
        }

        public Builder input(final Source input) {
            this.input = requireNonNull(input);
            return this;
        }

        public Builder jaxbContext(final JAXBContext context) {
            this.context = requireNonNull(context);
            return this;
        }

        public JarPatch build() {
            return build(patch, input,
                    null != context ? context : Diffs.jaxbContext());
        }

        private static JarPatch build(
                final Source patch,
                final Source input,
                final JAXBContext context) {
            requireNonNull(patch);
            requireNonNull(input);
            assert null != context;
            return new JarPatch() {
                @Override Source patch() { return patch; }
                @Override Source input() { return input; }
                @Override JAXBContext jaxbContext() { return context; }
            };
        }
    } // Builder
}

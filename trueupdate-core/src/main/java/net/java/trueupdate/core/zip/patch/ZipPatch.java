/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.core.zip.patch;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipFile;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import javax.annotation.WillClose;
import javax.annotation.concurrent.Immutable;
import net.java.trueupdate.core.io.FileStore;
import net.java.trueupdate.core.io.Job;
import net.java.trueupdate.core.io.Sink;
import net.java.trueupdate.core.zip.FileZipStore;
import net.java.trueupdate.core.zip.ZipInputTask;
import net.java.trueupdate.core.zip.ZipSource;
import net.java.trueupdate.core.zip.ZipSources;
import static net.java.trueupdate.shed.Objects.requireNonNull;

/**
 * Applies a diff archive to an input archive and writes an output archive.
 * Archives may be ZIP, JAR, EAR or WAR files.
 *
 * @author Christian Schlichtherle
 */
@Immutable
public abstract class ZipPatch {

    /** Returns a new builder for a ZIP patch. */
    public static Builder builder() { return new Builder(); }

    public abstract Job<Void, IOException> bindTo(File file);
    public abstract Job<Void, IOException> bindTo(Sink sink);

    public abstract void output(File file) throws IOException;
    public abstract void output(Sink sink) throws IOException;
    public abstract void output(@WillClose OutputStream out) throws IOException;

    /** A builder for a ZIP patch. */
    public static class Builder {

        private @CheckForNull ZipSource input, diff;
        private boolean createJar;

        Builder() { }

        public Builder input(final @Nullable File file) {
            return input(null == file ? null : new FileZipStore(file));
        }

        public Builder input(final @Nullable ZipSource input) {
            this.input = input;
            return this;
        }

        public Builder diff(final @Nullable File file) {
            return diff(null == file ? null : new FileZipStore(file));
        }

        public Builder diff(final @Nullable ZipSource diff) {
            this.diff = diff;
            return this;
        }

        public Builder createJar(final boolean createJar) {
            this.createJar = createJar;
            return this;
        }

        public ZipPatch build() {
            return create(input, diff, createJar);
        }

        private static ZipPatch create(
                final ZipSource input,
                final ZipSource diff,
                final boolean createJar) {
            requireNonNull(input);
            requireNonNull(diff);

            return new ZipPatch() {

                @Override public Job<Void, IOException> bindTo(File file) {
                    return bindTo(new FileStore(file));
                }

                @Override public Job<Void, IOException> bindTo(final Sink sink) {
                    return new Job<Void, IOException>() {
                        @Override public Void call() throws IOException {
                            output(sink);
                            return null;
                        }
                    };
                }

                @Override
                public void output(File file) throws IOException {
                    output(new FileStore(file));
                }

                @Override
                public void output(Sink sink) throws IOException {
                    output(sink.output());
                }

                @Override
                public void output(final @WillClose OutputStream out) throws IOException {
                    class InputTask implements ZipInputTask<Void, IOException> {
                        public Void execute(final ZipFile input) throws IOException {
                            class DiffTask implements ZipInputTask<Void, IOException> {
                                public Void execute(final ZipFile diff) throws IOException {
                                    final RawZipPatch rzp;
                                    if (createJar) {
                                        rzp = new RawJarPatch() {
                                            protected ZipFile input() { return input; }
                                            protected ZipFile diff() { return diff; }
                                        };
                                    } else {
                                        rzp = new RawZipPatch() {
                                            protected ZipFile input() { return input; }
                                            protected ZipFile diff() { return diff; }
                                        };
                                    }
                                    rzp.output(out);
                                    return null;
                                }
                            } // DiffTask
                            return ZipSources.execute(new DiffTask()).on(diff);
                        }
                    } // InputTask
                    ZipSources.execute(new InputTask()).on(input);
                }
            }; // ZipPatch
        }
    } // Builder
}

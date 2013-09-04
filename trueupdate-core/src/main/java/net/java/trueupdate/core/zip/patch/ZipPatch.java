/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.core.zip.patch;

import java.io.File;
import java.io.IOException;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import javax.annotation.WillClose;
import javax.annotation.WillNotClose;
import javax.annotation.concurrent.Immutable;
import net.java.trueupdate.core.io.Job;
import net.java.trueupdate.core.zip.ZipFileStore;
import net.java.trueupdate.core.zip.ZipInput;
import net.java.trueupdate.core.zip.ZipInputTask;
import net.java.trueupdate.core.zip.ZipOutput;
import net.java.trueupdate.core.zip.ZipSink;
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
    public abstract Job<Void, IOException> bindTo(ZipSink sink);

    public abstract void output(File file) throws IOException;
    public abstract void output(ZipSink sink) throws IOException;
    public abstract void output(@WillClose ZipOutput output) throws IOException;

    /** A builder for a ZIP patch. */
    public static class Builder {

        private @CheckForNull ZipSource input, diff;

        Builder() { }

        public Builder input(final @Nullable File file) {
            return input(null == file ? null : new ZipFileStore(file));
        }

        public Builder input(final @Nullable ZipSource input) {
            this.input = input;
            return this;
        }

        public Builder diff(final @Nullable File file) {
            return diff(null == file ? null : new ZipFileStore(file));
        }

        public Builder diff(final @Nullable ZipSource diff) {
            this.diff = diff;
            return this;
        }

        public ZipPatch build() { return create(input, diff); }

        private static ZipPatch create(
                final ZipSource input,
                final ZipSource diff) {
            requireNonNull(input);
            requireNonNull(diff);

            return new ZipPatch() {

                @Override public Job<Void, IOException> bindTo(File file) {
                    return bindTo(new ZipFileStore(file));
                }

                @Override public Job<Void, IOException> bindTo(final ZipSink sink) {
                    return new Job<Void, IOException>() {
                        @Override public Void call() throws IOException {
                            output(sink);
                            return null;
                        }
                    };
                }

                @Override
                public void output(File file) throws IOException {
                    output(new ZipFileStore(file));
                }

                @Override
                public void output(final ZipSink sink) throws IOException {
                    output(sink.output());
                }

                @Override
                public void output(final @WillClose ZipOutput output) throws IOException {
                    class InputTask implements ZipInputTask<Void, IOException> {
                        public Void execute(final @WillNotClose ZipInput input) throws IOException {
                            class DiffTask implements ZipInputTask<Void, IOException> {
                                public Void execute(final @WillNotClose ZipInput diff) throws IOException {
                                    new RawZipPatch() {
                                        protected ZipInput input() { return input; }
                                        protected ZipInput diff() { return diff; }
                                    }.output(output);
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

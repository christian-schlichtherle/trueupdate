/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.core.zip.patch;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.jar.JarFile;
import java.util.zip.ZipFile;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import javax.annotation.WillClose;
import javax.annotation.concurrent.Immutable;
import net.java.trueupdate.core.io.FileStore;
import net.java.trueupdate.core.io.Job;
import net.java.trueupdate.core.io.Sink;
import net.java.trueupdate.core.zip.ZipInputTask;
import net.java.trueupdate.core.zip.ZipSources;
import static net.java.trueupdate.shed.Objects.requireNonNull;

/**
 * Encapsulates a ZIP patch.
 *
 * @author Christian Schlichtherle
 */
@Immutable
public abstract class ZipPatchStatement {

    /** Returns a new builder for a ZIP patch statement. */
    public static Builder builder() { return new Builder(); }

    public abstract Job<Void, IOException> bindTo(File file);
    public abstract Job<Void, IOException> bindTo(Sink sink);

    public abstract void output(File file) throws IOException;
    public abstract void output(Sink sink) throws IOException;
    public abstract void output(@WillClose OutputStream out) throws IOException;

    /**
     * A builder for a ZIP patch statement.
     */
    public static class Builder {

        private @CheckForNull File input, diff;
        private boolean createJar;

        Builder() { }

        public Builder input(final @Nullable File input) {
            this.input = input;
            return this;
        }

        public Builder diff(final @Nullable File diff) {
            this.diff = diff;
            return this;
        }

        public Builder createJar(final boolean createJar) {
            this.createJar = createJar;
            return this;
        }

        public ZipPatchStatement build() {
            return create(input, diff, createJar);
        }

        private static ZipPatchStatement create(
                final File input,
                final File diff,
                final boolean createJar) {
            requireNonNull(input);
            requireNonNull(diff);

            return new ZipPatchStatement() {

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

                    class OnInputTask implements ZipInputTask<Void, IOException> {

                        @Override
                        public Void execute(final ZipFile input) throws IOException {

                            class OnDiffTask implements ZipInputTask<Void, IOException> {

                                @Override
                                public Void execute(final ZipFile diff) throws IOException {
                                    ZipPatch.builder()
                                            .input(input)
                                            .diff(diff)
                                            .createJar(createJar)
                                            .build()
                                            .output(out);
                                    return null;
                                }
                            } // OnDiffTask

                            return ZipSources.execute(new OnDiffTask())
                                             .on(new ZipFile(diff)); // always ZIP
                        }
                    } // OnInputTask

                    ZipSources.execute(new OnInputTask())
                              .on(newInputZipFile(input));
                }

                ZipFile newInputZipFile(File file) throws IOException {
                    return createJar
                            ? new JarFile(file, false)
                            : new ZipFile(file);
                }
            }; // ZipPatchStatement
        }
    } // Builder
}

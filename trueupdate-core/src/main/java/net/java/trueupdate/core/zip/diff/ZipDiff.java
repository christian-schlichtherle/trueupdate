/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.core.zip.diff;

import edu.umd.cs.findbugs.annotations.CreatesObligation;
import java.io.*;
import java.security.MessageDigest;
import java.util.zip.ZipFile;
import javax.annotation.*;
import javax.annotation.concurrent.Immutable;
import net.java.trueupdate.core.io.*;
import net.java.trueupdate.core.zip.*;
import static net.java.trueupdate.shed.Objects.requireNonNull;

/**
 * Compares two archives entry by entry.
 * Archives may be ZIP, JAR, EAR or WAR files.
 *
 * @author Christian Schlichtherle
 */
@Immutable
public abstract class ZipDiff {

    /** Returns a new builder for a ZIP diff. */
    public static Builder builder() { return new Builder(); }

    public abstract Job<Void, IOException> bindTo(File file);
    public abstract Job<Void, IOException> bindTo(Sink sink);

    public abstract void output(File file) throws IOException;
    public abstract void output(Sink sink) throws IOException;
    public abstract void output(@WillClose OutputStream out) throws IOException;

    /**
     * A builder for a ZIP diff.
     * The default message digest is SHA-1.
     */
    public static class Builder {

        private @CheckForNull ZipSource input1, input2;
        private @CheckForNull String digest;

        Builder() { }

        public Builder input1(final @CheckForNull File input1) {
            return input1(null == input1 ? null : new FileZipStore(input1));
        }

        public Builder input1(final @Nullable ZipSource input1) {
            this.input1 = input1;
            return this;
        }

        public Builder input2(final @CheckForNull File input2) {
            return input2(null == input2 ? null : new FileZipStore(input2));
        }

        public Builder input2(final @Nullable ZipSource input2) {
            this.input2 = input2;
            return this;
        }

        public Builder digest(final @Nullable String digest) {
            this.digest = digest;
            return this;
        }

        public ZipDiff build() {
            return create(input1, input2, digest);
        }

        private static @CreatesObligation
        ZipDiff create(
                final ZipSource input1,
                final ZipSource input2,
                final @CheckForNull String digestName) {
            requireNonNull(input1);
            requireNonNull(input2);

            return new ZipDiff() {

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
                    class Input1Task implements ZipInputTask<Void, IOException> {
                        public Void execute(final ZipFile input1) throws IOException {
                            class Input2Task implements ZipInputTask<Void, IOException> {
                                public Void execute(final ZipFile input2) throws IOException {
                                    new RawZipDiff() {
                                        final MessageDigest digest = MessageDigests.create(
                                                null != digestName ? digestName : "SHA-1");

                                        protected ZipFile input1() { return input1; }
                                        protected ZipFile input2() { return input2; }
                                        protected MessageDigest digest() { return digest; }
                                    }.output(out);
                                    return null;
                                }
                            } // Input2Task
                            return ZipSources.execute(new Input2Task()).on(input2);
                        }
                    } // Input1Task
                    ZipSources.execute(new Input1Task()).on(input1);
                }
            }; // ZipDiff
        }
    } // Builder
}

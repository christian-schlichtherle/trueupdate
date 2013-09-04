/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.core.zip.diff;

import edu.umd.cs.findbugs.annotations.CreatesObligation;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.util.zip.ZipFile;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import javax.annotation.WillClose;
import javax.annotation.concurrent.Immutable;
import net.java.trueupdate.core.io.FileStore;
import net.java.trueupdate.core.io.Job;
import net.java.trueupdate.core.io.MessageDigests;
import net.java.trueupdate.core.io.Sink;
import net.java.trueupdate.core.io.ZipInputTask;
import net.java.trueupdate.core.io.ZipSources;
import static net.java.trueupdate.shed.Objects.requireNonNull;

/**
 * Encapsulates a ZIP diff.
 *
 * @author Christian Schlichtherle
 */
@Immutable
public abstract class ZipDiffStatement {

    /** Returns a new builder for a ZIP diff statement. */
    public static Builder builder() { return new Builder(); }

    public abstract Job<Void, IOException> bindTo(File file);
    public abstract Job<Void, IOException> bindTo(Sink sink);

    public abstract void output(File file) throws IOException;
    public abstract void output(Sink sink) throws IOException;
    public abstract void output(@WillClose OutputStream out) throws IOException;

    /**
     * A builder for a ZIP diff statement.
     * The default message digest is SHA-1.
     */
    public static class Builder {

        private @CheckForNull File input1, input2;
        private @CheckForNull String digest;

        Builder() { }

        public Builder input1(final @Nullable File input1) {
            this.input1 = input1;
            return this;
        }

        public Builder input2(final @Nullable File input2) {
            this.input2 = input2;
            return this;
        }

        public Builder digest(final @Nullable String digest) {
            this.digest = digest;
            return this;
        }

        public ZipDiffStatement build() {
            return create(input1, input2, digest);
        }

        private static @CreatesObligation ZipDiffStatement create(
                final File input1,
                final File input2,
                final @CheckForNull String digest) {
            requireNonNull(input1);
            requireNonNull(input2);

            return new ZipDiffStatement() {

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

                    class OnInput1Task implements ZipInputTask<Void, IOException> {

                        @Override
                        public Void execute(final ZipFile input1) throws IOException {

                            class OnInput2Task implements ZipInputTask<Void, IOException> {

                                @Override
                                public Void execute(final ZipFile input2) throws IOException {
                                    RawZipDiff.builder()
                                            .input1(input1)
                                            .input2(input2)
                                            .digest(nonNullOrSha1(digest))
                                            .build()
                                            .output(out);
                                    return null;
                                }

                                MessageDigest nonNullOrSha1(@CheckForNull String digest) {
                                    return MessageDigests.create(
                                            null != digest ? digest : "SHA-1");
                                }
                            } // OnInput2Task

                            return ZipSources.execute(new OnInput2Task())
                                             .on(new ZipFile(input2));
                        }
                    } // OnInput1Task

                    ZipSources.execute(new OnInput1Task())
                              .on(new ZipFile(input1));
                }
            }; // ZipDiffStatement
        }
    } // Builder
}

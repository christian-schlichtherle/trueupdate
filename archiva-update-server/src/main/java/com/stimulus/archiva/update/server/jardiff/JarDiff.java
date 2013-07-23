package com.stimulus.archiva.update.server.jardiff;

import javax.annotation.WillNotClose;
import javax.annotation.concurrent.Immutable;
import static java.util.Objects.requireNonNull;
import java.util.concurrent.Callable;
import java.util.jar.JarFile;

/**
 * Diffs two JAR files.
 *
 * @author Christian Schlichtherle
 */
@Immutable
public final class JarDiff implements Callable<Void> {

    private final Parameters parameters;

    /**
     * Constructs a JAR diff.
     *
     * @param parameters the JAR diff parameters.
     */
    public JarDiff(final Parameters parameters) {
        this.parameters = requireNonNull(parameters);
    }

    /** Returns the JAR diff parameters. */
    public Parameters parameters() { return parameters; }

    private JarFile file1() { return parameters().file1(); }
    private JarFile file2() { return parameters().file2(); }

    /** Computes the diff and calls the hook methods where appropriate. */
    public Void call() throws Exception {
        return null;
    }

    /**
     * The JAR diff parameters.
     * Implementations should be immutable.
     */
    public interface Parameters {

        /** Returns the first JAR file. */
        @WillNotClose JarFile file1();

        /** Returns the second JAR file. */
        @WillNotClose JarFile file2();

        /** A builder for JAR diff parameters. */
        class Builder {

            private @WillNotClose JarFile file1, file2;

            public Builder file1(final @WillNotClose JarFile file1) {
                this.file1 = requireNonNull(file1);
                return this;
            }

            public Builder file2(final @WillNotClose JarFile file2) {
                this.file2 = requireNonNull(file2);
                return this;
            }

            public Parameters build() {
                return build(file1, file2);
            }

            private Parameters build(
                    final @WillNotClose JarFile file1,
                    final @WillNotClose JarFile file2) {
                requireNonNull(file1);
                requireNonNull(file2);
                return new Parameters() {
                    @Override public JarFile file1() { return file1; }
                    @Override public JarFile file2() { return file2; }
                };
            }
        }
    }
}

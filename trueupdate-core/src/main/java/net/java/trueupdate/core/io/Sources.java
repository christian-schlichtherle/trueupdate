/*
 * Copyright (C) 2005-2013 Schlichtherle IT Services.
 * Copyright (C) 2013 Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.core.io;

import java.io.*;
import javax.annotation.CheckForNull;
import javax.annotation.concurrent.Immutable;
import net.java.trueupdate.util.Objects;

/**
 * Provides functions for {@link Source}s.
 *
 * @author Christian Schlichtherle (copied and edited from TrueLicense Core 2.3.1)
 */
@Immutable
public class Sources {

    /**
     * Returns a source which loads the resource with the given {@code name}.
     * If the given class loader is not {@code null}, then the resource will
     * get loaded as described in
     * {@link ClassLoader#getResourceAsStream(String)}.
     * Otherwise, the resource will get loaded as described in
     * {@link ClassLoader#getSystemResourceAsStream(String)}.
     *
     * @param  name the name of the resource to load.
     * @param  loader the nullable class loader to use for loading the resource.
     *         If this is {@code null}, then the system class loader will get
     *         used.
     * @return A source which loads the resource with the given {@code name}.
     */
    public static Source forResource(
            final String name,
            final @CheckForNull ClassLoader loader) {
        Objects.requireNonNull(name);
        return new Source() {
            @Override public InputStream input() throws IOException {
                return check(null != loader
                        ? loader.getResourceAsStream(name)
                        : ClassLoader.getSystemResourceAsStream(name), name);
            }

            InputStream check(final @CheckForNull InputStream in,
                              final String name)
            throws FileNotFoundException {
                if (null == in) throw new FileNotFoundException(String.format(
                        "Cannot locate resource %s on the class path.", name));
                return in;
            }
        };
    }

    public static <V, X extends Exception>
            ExecuteStatement<V, X> execute(InputTask<V, X> task) {
        return new WithInputTask<V, X>(task);
    }

    public interface ExecuteStatement<V, X extends Exception> {
        V on(File file) throws X, IOException;
        V on(Source source) throws X, IOException;
    }

    private Sources() { }
}

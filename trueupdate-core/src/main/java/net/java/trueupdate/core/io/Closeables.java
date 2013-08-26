package net.java.trueupdate.core.io;

import java.io.Closeable;
import java.io.IOException;

/**
 * Provides functions for {@link Closeable}s.
 *
 * @author Christian Schlichtherle
 */
final class Closeables {

    static <V, R extends Closeable, X extends Exception>
            V execute(final Task<V, R, X> task, final R resource)
    throws X, IOException {
        X ex = null;
        try {
            return task.execute(resource);
        } catch (Exception ex2) {
            throw ex = (X) ex2;
        } finally {
            try {
                resource.close();
            } catch (IOException ex2) {
                if (null == ex) throw ex2;
            }
        }
    }

    private Closeables() { }
}

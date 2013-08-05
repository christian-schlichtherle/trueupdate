/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.jax.rs;

import java.util.concurrent.Callable;

/**
 * Provides utility methods for update services.
 *
 * @see UpdateService
 * @see ConfiguredUpdateService
 * @author Christian Schlichtherle
 */
final class UpdateServices {

    private static final int BAD_REQUEST = 400, NOT_FOUND = 404;

    private UpdateServices() { }

    static <V> V wrap(final Callable<V> task) throws UpdateServiceException {
        try {
            return task.call();
        } catch (UpdateServiceException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            throw new UpdateServiceException(BAD_REQUEST, ex);
        } catch (Exception ex) {
            throw new UpdateServiceException(NOT_FOUND, ex);
        }
    }
}

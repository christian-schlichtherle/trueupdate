/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.jaxrs.server;

import java.util.concurrent.Callable;
import net.java.trueupdate.jaxrs.util.UpdateServiceException;

/**
 * Utility functions for update servers.
 *
 * @see AbstractUpdateServer
 * @see ConfiguredUpdateServer
 * @author Christian Schlichtherle
 */
final class UpdateServers {

    private static final int BAD_REQUEST = 400, NOT_FOUND = 404;

    private UpdateServers() { }

    static <V> V wrap(final Callable<V> task)
    throws UpdateServiceException {
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

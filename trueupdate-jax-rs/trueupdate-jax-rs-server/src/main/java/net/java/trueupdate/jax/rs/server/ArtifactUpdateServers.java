/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.jax.rs.server;

import net.java.trueupdate.jax.rs.util.ArtifactUpdateServiceException;

import java.util.concurrent.Callable;

/**
 * Utility functions for update servers.
 *
 * @see BasicArtifactUpdateServer
 * @see ConfiguredArtifactUpdateServer
 * @author Christian Schlichtherle
 */
final class ArtifactUpdateServers {

    private static final int BAD_REQUEST = 400, NOT_FOUND = 404;

    private ArtifactUpdateServers() { }

    static <V> V wrap(final Callable<V> task) throws ArtifactUpdateServiceException {
        try {
            return task.call();
        } catch (ArtifactUpdateServiceException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            throw new ArtifactUpdateServiceException(BAD_REQUEST, ex);
        } catch (Exception ex) {
            throw new ArtifactUpdateServiceException(NOT_FOUND, ex);
        }
    }
}

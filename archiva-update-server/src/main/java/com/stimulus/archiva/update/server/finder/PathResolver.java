/*
 * Copyright (C) 2005-2013 Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.server.finder;

import java.nio.file.Path;

/**
 * Resolves paths to artifacts and their updates.
 *
 * @author Christian Schlichtherle
 */
public interface PathResolver {

    /**
     * Resolves the path to the described artifact.
     *
     * @param  descriptor the descriptor for the artifact.
     * @return the path to the described artifact.
     * @throws Exception if resolving the artifact is not possible for some
     *         (other) reason.
     */
    Path resolveArtifactPath(ArtifactDescriptor descriptor) throws Exception;

    /**
     * Resolves the path to the latest update for the described artifact.
     *
     * @param  descriptor the descriptor for the artifact.
     * @return the path to the latest update for the described artifact.
     * @throws ArtifactUpToDateException if the described artifact is already
     *         up-to-date.
     * @throws Exception if resolving the update is not possible for some
     *         (other) reason.
     */
    Path resolveUpdatePath(ArtifactDescriptor descriptor) throws Exception;
}

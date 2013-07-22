/*
 * Copyright (C) 2005-2013 Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.server;

import com.stimulus.archiva.update.commons.ArtifactDescriptor;

import java.nio.file.Path;

/**
 * Resolves paths to described artifacts and their latest update.
 *
 * @author Christian Schlichtherle
 */
public interface ArtifactResolver {

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
     * Resolves the descriptor for the latest update for the described artifact.
     *
     * @param  descriptor the descriptor for the artifact.
     * @return the descriptor for the latest update for the described artifact.
     * @throws Exception if resolving the update is not possible for some
     *         (other) reason.
     */
    ArtifactDescriptor resolveUpdateDescriptor(ArtifactDescriptor descriptor)
    throws Exception;
}

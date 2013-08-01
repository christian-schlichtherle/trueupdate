/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.core.artifact;

import java.io.File;

/**
 * Resolves paths to described artifacts and their latest update.
 *
 * @author Christian Schlichtherle
 */
public interface ArtifactResolver {

    /**
     * Resolves the file containing the described artifact.
     *
     * @param  descriptor the descriptor for the artifact.
     * @return the file containing the described artifact.
     * @throws Exception if resolving the artifact file is not possible for
     *         some reason.
     */
    File resolveArtifactFile(ArtifactDescriptor descriptor) throws Exception;

    /**
     * Resolves the descriptor for the latest update for the described artifact.
     *
     * @param  descriptor the descriptor for the artifact.
     * @return the descriptor for the latest update for the described artifact.
     * @throws Exception if resolving the update descriptor is not possible for
     *         some reason.
     */
    ArtifactDescriptor resolveUpdateDescriptor(ArtifactDescriptor descriptor)
    throws Exception;
}

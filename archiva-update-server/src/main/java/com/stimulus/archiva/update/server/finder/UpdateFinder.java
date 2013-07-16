/*
 * Copyright (C) 2005-2013 Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.server.finder;

import java.nio.file.Path;

/**
 * Finds the latest version of a given artifact.
 *
 * @author Christian Schlichtherle
 */
public interface UpdateFinder {

    /**
     * Returns the path to the latest version of the artifact with the given
     * Maven coordinates.
     *
     * @param  descriptor the descriptor for the artifact.
     * @return the path to the latest version of the artifact with the given
     *         Maven coordinates.
     * @throws UpToDateException if the described artifact is already up-to-date.
     * @throws Exception if finding an update is not possible for some (other)
     *         reason.
     */
    Path findUpdatePath(ArtifactDescriptor descriptor) throws Exception;
}

/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.artifact.spec;

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
     * Resolves the latest update version for the described artifact.
     *
     * @param  descriptor the descriptor for the artifact.
     * @return the latest update version for the described artifact.
     * @throws Exception if resolving the latest update version is not possible
     *         for some reason.
     */
    String resolveUpdateVersion(ArtifactDescriptor descriptor) throws Exception;
}

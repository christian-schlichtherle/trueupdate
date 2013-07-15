/*
 * Copyright (C) 2005-2013 Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.autoupdate.server;

import java.nio.file.Path;

/**
 * Resolves a path to an artifact.
 *
 * @author Christian Schlichtherle
 */
public interface ArtifactResolver {

    /**
     * Resolves the path to an artifact with the given Maven coordinates.
     *
     * @param groupId the group id.
     * @param artifactId the artifact id.
     * @param version the version.
     * @param type the type.
     * @return the path to the artifact.
     */
    Path resolve(String groupId, String artifactId, String version, String type);
}

/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.jax.rs.client;

import com.sun.jersey.core.util.MultivaluedMapImpl;
import javax.ws.rs.core.MultivaluedMap;
import net.java.trueupdate.core.artifact.ArtifactDescriptor;

/**
 * Utility functions for {@link ArtifactDescriptor}s.
 *
 * @author Christian Schlichtherle
 */
final class ArtifactDescriptors {

    private ArtifactDescriptors() { }

    /**
     * Returns a multi valued map with query parameters for the given artifact
     * descriptor.
     *
     * @param descriptor the artifact descriptor.
     * @return the multi valued map with the query parameters.
     */
    static MultivaluedMap<String, String> queryParameters(
            final ArtifactDescriptor descriptor) {
        final MultivaluedMap<String, String> map = new MultivaluedMapImpl();
        map.putSingle("groupId", descriptor.groupId());
        map.putSingle("artifactId", descriptor.artifactId());
        map.putSingle("version", descriptor.version());
        map.putSingle("classifier", descriptor.classifier());
        map.putSingle("extension", descriptor.extension());
        return map;
    }
}

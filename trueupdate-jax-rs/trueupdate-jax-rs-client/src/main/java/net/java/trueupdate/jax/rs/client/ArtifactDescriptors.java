/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.jax.rs.client;

import com.sun.jersey.core.util.MultivaluedMapImpl;
import java.util.concurrent.Callable;
import javax.ws.rs.core.MultivaluedMap;
import net.java.trueupdate.artifact.api.ArtifactDescriptor;

/**
 * Utility functions for {@link ArtifactDescriptor}s.
 *
 * @author Christian Schlichtherle
 */
final class ArtifactDescriptors {

    private ArtifactDescriptors() { }

    /**
     * Returns a new multi valued map with query parameters for the given
     * artifact descriptor.
     *
     * @param descriptor the artifact descriptor.
     * @return the new multi valued map with the query parameters.
     */
    static MultivaluedMap<String, String> queryParameters(
            final ArtifactDescriptor descriptor) {

        class QueryParameters implements Callable<MultivaluedMap<String, String>> {

            final MultivaluedMap<String, String> map = new MultivaluedMapImpl();

            @SuppressWarnings("ReturnOfCollectionOrArrayField")
            @Override public MultivaluedMap<String, String> call() {
                put("groupId", descriptor.groupId());
                put("artifactId", descriptor.artifactId());
                put("version", descriptor.version());
                put("classifier", descriptor.classifier());
                put("extension", descriptor.extension());
                return map;
            }

            void put(String key, String value) {
                if (!value.isEmpty()) map.putSingle(key, value);
            }
        } // QueryParameters

        return new QueryParameters().call();
    }
}

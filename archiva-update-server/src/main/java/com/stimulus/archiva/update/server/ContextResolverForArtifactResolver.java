/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.server;

import com.stimulus.archiva.update.core.artifact.ArtifactResolver;
import javax.annotation.concurrent.ThreadSafe;
import javax.ws.rs.ext.*;

/**
 * A context resolver which resolves {@link ArtifactResolver}s to a singleton
 * {@link ConfiguredMavenArtifactResolver}.
 */
@ThreadSafe
@Provider
public class ContextResolverForArtifactResolver
implements ContextResolver<ArtifactResolver> {

    private volatile ArtifactResolver artifactResolver;

    @Override public ArtifactResolver getContext(Class<?> type) {
        final ArtifactResolver artifactResolver = this.artifactResolver;
        return null != artifactResolver
                ? artifactResolver
                : (this.artifactResolver = new ConfiguredMavenArtifactResolver());
    }
}

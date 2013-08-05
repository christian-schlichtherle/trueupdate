/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.server;

import com.stimulus.archiva.update.core.artifact.ArtifactResolver;
import com.stimulus.archiva.update.maven.*;
import javax.annotation.concurrent.ThreadSafe;
import javax.ws.rs.ext.*;

/**
 * A context resolver which resolves {@link ArtifactResolver}s to a singleton
 * {@link MavenArtifactResolver}.
 */
@ThreadSafe
@Provider
public class ContextResolverForArtifactResolver
implements ContextResolver<ArtifactResolver> {

    @Override public ArtifactResolver getContext(Class<?> type) {
        return MavenArtifactResolver.getInstance();
    }
}

/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.server.impl.maven;

import javax.annotation.Nullable;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.ContextResolver;
import net.java.trueupdate.artifact.spec.ArtifactResolver;
import net.java.trueupdate.jax.rs.server.BasicUpdateServer;
import net.java.trueupdate.shed.Objects;

/**
 * A an artifact update server which uses a maven artifact resolver.
 *
 * @author Christian Schlichtherle
 */
@Path("/")
public final class MavenUpdateServer extends BasicUpdateServer {

    private @Nullable ArtifactResolver artifactResolver;

    /**
     * Constructs a maven update server with the given configuration URI.
     */
    public MavenUpdateServer(final ArtifactResolver artifactResolver) {
        this.artifactResolver = Objects.requireNonNull(artifactResolver);
    }

    /**
     * Constructs a maven update server.
     * Use of this constructor requires calling {@link #setContextResolver}
     * before use.
     */
    public MavenUpdateServer() { }

    /**
     * Calling this method is required when using the no-arg constructor.
     * This method looks up the artifact resolver from the context resolver
     * with a {@code null} type parameter.
     */
    @Context
    public void setContextResolver(
            ContextResolver<ArtifactResolver> contextResolver) {
        this.artifactResolver = contextResolver.getContext(null);
    }

    @Override
    protected ArtifactResolver artifactResolver() { return artifactResolver; }
}

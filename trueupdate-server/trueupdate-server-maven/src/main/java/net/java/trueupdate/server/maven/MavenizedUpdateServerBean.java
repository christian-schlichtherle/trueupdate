/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.server.maven;

import javax.ejb.*;
import javax.inject.Provider;
import javax.ws.rs.Path;
import net.java.trueupdate.artifact.api.ArtifactResolver;
import net.java.trueupdate.jax.rs.server.BasicUpdateServer;

/**
 * A an artifact update server which uses a maven artifact resolver.
 *
 * @author Christian Schlichtherle
 */
@Stateless
@Path("/")
public class MavenizedUpdateServerBean extends BasicUpdateServer {

    @EJB
    private Provider<ArtifactResolver> artifactResolverProvider;

    @Override protected ArtifactResolver artifactResolver() {
        return artifactResolverProvider.get();
    }
}

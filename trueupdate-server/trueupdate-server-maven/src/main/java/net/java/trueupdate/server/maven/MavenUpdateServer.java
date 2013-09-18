/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.server.maven;

import javax.annotation.concurrent.Immutable;
import javax.ws.rs.Path;
import net.java.trueupdate.artifact.spec.ArtifactResolver;
import net.java.trueupdate.core.io.Sources;
import net.java.trueupdate.jaxrs.server.BasicUpdateServer;

/**
 * An artifact update server which uses a maven artifact resolver.
 * For best performance, this should be used like a singleton
 * - see {@link MavenUpdateServerApplication}.
 *
 * @author Christian Schlichtherle
 */
@Path("/")
@Immutable
public final class MavenUpdateServer extends BasicUpdateServer {

    private final ArtifactResolver artifactResolver;

    public MavenUpdateServer() {
        try {
            this.artifactResolver = ServerParameters
                    .decodeFromXml(
                        Sources.forResource(
                            "META-INF/update/server.xml",
                            Thread.currentThread().getContextClassLoader()))
                    .mavenArtifactResolver();
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    protected ArtifactResolver artifactResolver() { return artifactResolver; }
}

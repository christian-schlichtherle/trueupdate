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
import net.java.trueupdate.server.maven.config.UpdateServerConfiguration;

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

    private static final String CONFIGURATION = "META-INF/update/server.xml";

    private final ArtifactResolver artifactResolver;

    public MavenUpdateServer() {
        try {
            this.artifactResolver = parameters().artifactResolver();
        } catch (Exception ex) {
            throw new IllegalStateException(String.format(
                    "Failed to load configuration from %s .", CONFIGURATION),
                    ex);
        }
    }

    private static UpdateServerParameters parameters() throws Exception {
        return UpdateServerParameters.builder().parse(configuration()).build();
    }

    private static UpdateServerConfiguration configuration() throws Exception {
        return UpdateServerConfiguration.decodeFromXml(
                Sources.forResource(CONFIGURATION,
                    Thread.currentThread().getContextClassLoader()));
    }

    @Override
    protected ArtifactResolver artifactResolver() { return artifactResolver; }
}

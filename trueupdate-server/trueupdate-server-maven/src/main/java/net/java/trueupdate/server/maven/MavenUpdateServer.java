/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.server.maven;

import javax.annotation.concurrent.Immutable;
import javax.ws.rs.Path;
import javax.xml.bind.JAXBContext;
import net.java.trueupdate.artifact.spec.ArtifactResolver;
import net.java.trueupdate.jaxrs.server.BasicUpdateServer;
import net.java.trueupdate.server.maven.ci.UpdateServerCi;

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

    private static UpdateServerCi configuration() throws Exception {
        return (UpdateServerCi) JAXBContext
                .newInstance(UpdateServerCi.class)
                .createUnmarshaller()
                .unmarshal(Thread
                    .currentThread()
                    .getContextClassLoader()
                    .getResource(CONFIGURATION));
    }

    @Override
    protected ArtifactResolver artifactResolver() { return artifactResolver; }
}

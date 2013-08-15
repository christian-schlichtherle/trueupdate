/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.server.impl.maven;

import java.lang.reflect.UndeclaredThrowableException;
import java.net.*;
import javax.annotation.Resource;
import javax.ejb.*;
import javax.ws.rs.Path;
import net.java.trueupdate.artifact.impl.maven.MavenArtifactResolver;
import net.java.trueupdate.artifact.spec.ArtifactResolver;
import net.java.trueupdate.core.io.*;
import net.java.trueupdate.core.util.SystemProperties;
import net.java.trueupdate.jax.rs.server.BasicArtifactUpdateServer;

/**
 * A an artifact update server which uses a maven artifact resolver.
 *
 * @author Christian Schlichtherle
 */
@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
@Path("/")
public class ArtifactUpdateServerBean extends BasicArtifactUpdateServer {

    @Resource(name = "configurationUri")
    private String configurationString = "net/java/trueupdate/artifact/impl/maven/main-repositories.xml";

    private volatile ArtifactResolver artifactResolver;

    @Override protected ArtifactResolver artifactResolver() {
        final ArtifactResolver ar = artifactResolver;
        return null != ar ? ar : (artifactResolver = loadArtifactResolver());
    }

    private synchronized ArtifactResolver loadArtifactResolver() {
        if (null != artifactResolver) // DCL w/ volatile is OK
            return artifactResolver;
        try { return MavenArtifactResolver.decodeFromXml(source()); }
        catch (RuntimeException ex) { throw ex; }
        catch (Exception ex) { throw new UndeclaredThrowableException(ex); }
    }

    private Source source() throws Exception {
        final URI uri = configurationUri();
        return uri.isAbsolute()
                ? Sources.forUrl(uri.toURL())
                : Sources.forResource(removeLeadingSlashes(uri.getPath()),
                                      contextClassLoader());
    }

    private URI configurationUri() throws URISyntaxException {
        return new URI(SystemProperties.resolve(configurationString));
    }

    private static String removeLeadingSlashes(String string) {
        while (string.startsWith("/")) string = string.substring(1);
        return string;
    }

    private static ClassLoader contextClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }
}

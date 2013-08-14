/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.server.impl.maven;

import java.net.URI;
import javax.annotation.concurrent.Immutable;
import javax.naming.InitialContext;
import javax.ws.rs.ext.*;

import net.java.trueupdate.artifact.impl.maven.MavenArtifactResolver;
import net.java.trueupdate.artifact.spec.ArtifactResolver;
import net.java.trueupdate.core.io.*;
import net.java.trueupdate.core.util.SystemProperties;

/**
 * A context resolver which resolves {@link ArtifactResolver}s to a singleton
 * {@link net.java.trueupdate.artifact.impl.maven.MavenArtifactResolver}.
 */
@Immutable
@Provider
public class ContextResolverForArtifactResolver
implements ContextResolver<ArtifactResolver> {

    @Override public ArtifactResolver getContext(Class<?> type) {
        return Lazy.INSTANCE;
    }

    private static class Lazy {

        static final MavenArtifactResolver INSTANCE;

        static {
            try {
                INSTANCE = configuredMavenArtifactResolver();
            } catch (Exception ex) {
                throw new ExceptionInInitializerError(ex);
            }
        }

        static MavenArtifactResolver configuredMavenArtifactResolver()
        throws Exception {
            return MavenArtifactResolver.decodeFromXml(source());
        }

        static Source source() throws Exception {
            final URI uri = configurationUri();
            return uri.isAbsolute()
                    ? Sources.forUrl(uri.toURL())
                    : Sources.forResource(removeLeadingSlashes(uri.getPath()),
                                          contextClassLoader());
        }

        static URI configurationUri() throws Exception {
            return new URI(SystemProperties.resolve(
                    (String) InitialContext.doLookup(
                        "java:comp/env/maven.repositories.configuration.uri")));
        }

        static String removeLeadingSlashes(String string) {
            while (string.startsWith("/")) string = string.substring(1);
            return string;
        }

        static ClassLoader contextClassLoader() {
            return Thread.currentThread().getContextClassLoader();
        }
    }
}

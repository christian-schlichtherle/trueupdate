/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.server;

import java.net.URI;
import javax.annotation.concurrent.ThreadSafe;
import javax.naming.InitialContext;
import javax.ws.rs.ext.*;

import net.java.trueupdate.repository.spec.ArtifactRepository;
import net.java.trueupdate.core.io.*;
import net.java.trueupdate.core.util.SystemProperties;
import net.java.trueupdate.maven.*;
import net.java.trueupdate.maven.model.Repositories;

/**
 * A context resolver which resolves {@link net.java.trueupdate.repository.spec.ArtifactRepository}s to a singleton
 * {@link net.java.trueupdate.maven.MavenArtifactRepository}.
 */
@ThreadSafe
@Provider
public class ContextResolverForArtifactResolver
implements ContextResolver<ArtifactRepository> {

    @Override public ArtifactRepository getContext(Class<?> type) {
        return Lazy.INSTANCE;
    }

    private static class Lazy {

        static final MavenArtifactRepository INSTANCE;

        static {
            try {
                INSTANCE = configuredMavenArtifactResolver();
            } catch (Exception ex) {
                throw new ExceptionInInitializerError(ex);
            }
        }

        static MavenArtifactRepository configuredMavenArtifactResolver()
        throws Exception {
            return new MavenArtifactRepositoryAdapter().unmarshal(repositories());
        }

        static Repositories repositories() throws Exception {
            return Repositories.decodeFromXml(source());
        }

        static Source source() throws Exception {
            final URI uri = configurationUri();
            return uri.isAbsolute()
                    ? Sources.forUrl(uri.toURL())
                    : Sources.forResource(removeLeadingSlashes(uri.getPath()),
                        Thread.currentThread().getContextClassLoader());
        }

        static URI configurationUri() throws Exception {
            return new URI(SystemProperties.resolve(
                    (String) InitialContext.doLookup(
                        "java:comp/env/maven-repositories/configuration-uri")));
        }

        static String removeLeadingSlashes(String string) {
            while (string.startsWith("/")) string = string.substring(1);
            return string;
        }
    }
}

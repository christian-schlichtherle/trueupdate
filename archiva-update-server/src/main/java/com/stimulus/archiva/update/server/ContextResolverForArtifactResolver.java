/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.server;

import com.stimulus.archiva.update.core.artifact.ArtifactResolver;
import com.stimulus.archiva.update.core.io.*;
import com.stimulus.archiva.update.maven.*;
import com.stimulus.archiva.update.maven.model.Repositories;
import java.net.URI;
import javax.annotation.concurrent.ThreadSafe;
import javax.naming.InitialContext;
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
            return new MavenArtifactResolverAdapter().unmarshal(repositories());
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
            return new URI((String) InitialContext.doLookup(
                    "java:comp/env/repositories/configuration-uri"));
        }

        static String removeLeadingSlashes(String string) {
            while (string.startsWith("/")) string = string.substring(1);
            return string;
        }
    }
}

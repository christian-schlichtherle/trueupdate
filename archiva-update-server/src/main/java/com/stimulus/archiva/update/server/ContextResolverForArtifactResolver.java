/*
 * Copyright (C) 2005-2013 Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.server;

import com.stimulus.archiva.update.core.artifact.ArtifactResolver;
import com.stimulus.archiva.update.maven.MavenArtifactResolver;
import org.eclipse.aether.repository.LocalRepository;
import javax.annotation.concurrent.ThreadSafe;
import javax.ws.rs.ext.*;

/**
 * A context resolver which resolves {@link ArtifactResolver}s to a singleton
 * {@link com.stimulus.archiva.update.maven.MavenArtifactResolver}.
 */
@ThreadSafe
@Provider
public class ContextResolverForArtifactResolver
implements ContextResolver<ArtifactResolver> {

    private volatile ArtifactResolver artifactResolver;

    @Override public ArtifactResolver getContext(final Class<?> type) {
        final ArtifactResolver ar = artifactResolver;
        return null != ar ? ar : (artifactResolver = artifactResolver());
    }

    private static ArtifactResolver artifactResolver() {
        return new MavenArtifactResolver(localRepository());
    }

    static LocalRepository localRepository() {
        return new LocalRepository(
                System.getProperty(propertyKey(), defaultValue()));
    }

    static String propertyKey() {
        return packageName() + ".repository";
    }

    private static String packageName() {
        return ContextResolverForArtifactResolver.class.getPackage().getName();
    }

    private static String defaultValue() {
        return userHome() + "/.m2/repository";
    }

    private static String userHome() { return System.getProperty("user.home"); }
}

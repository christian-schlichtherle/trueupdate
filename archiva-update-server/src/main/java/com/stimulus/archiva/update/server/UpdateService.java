/*
 * Copyright (C) 2005-2013 Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.server;

import com.stimulus.archiva.update.server.resolver.ArtifactDescriptor;
import com.stimulus.archiva.update.server.resolver.ArtifactResolver;
import java.util.Objects;
import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import static javax.ws.rs.core.MediaType.*;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Providers;

/**
 * An (unconfigured) update service for *Archiva products.
 *
 * @author Christian Schlichtherle
 */
@Path("update")
@Immutable
public final class UpdateService {

    private final ArtifactResolver resolver;

    /**
     * Constructs an update service.
     * This constructor immediately resolves the artifact resolver by
     * looking up a {@code ContextResolver<ArtifactResolver>} in the
     * given providers.
     * You can provide a context resolver for this class like this:
     * <pre>{@code
     * package ...;
     *
     * import javax.ws.rs.ext.*;
     * import com.stimulus.archiva.update.server.resolver.ArtifactResolver;
     *
     * &#64;Provider
     * public class ArtifactResolverResolver
     * implements ContextResolver<ArtifactResolver> {
     *     &#64;Override public ArtifactResolver getContext(Class<?> type) {
     *         return ...;
     *     }
     * }
     * }</pre>
     *
     * @param providers the providers.
     */
    public UpdateService(@Context Providers providers) {
        this(resolver(providers));
    }

    private static ArtifactResolver resolver(final Providers providers) {
        final ContextResolver<ArtifactResolver>
                resolver = providers.getContextResolver(ArtifactResolver.class, WILDCARD_TYPE);
        if (null == resolver)
            throw new IllegalArgumentException("No @Provider annotated ContextResolver<ArtifactResolver> available.");
        return resolver.getContext(ArtifactResolver.class);
    }

    /**
     * Constructs an update service.
     * This is the preferable constructor with Dependency Injection frameworks,
     * e.g. CDI, Spring or Guice.
     *
     * @param resolver the artifact resolver.
     */
    @Inject
    public UpdateService(final ArtifactResolver resolver) {
        this.resolver = Objects.requireNonNull(resolver);
    }

    /** Returns a configured update service. */
    @Path("/")
    public ConfiguredUpdateService configure(
            final @QueryParam("groupId") String groupId,
            final @QueryParam("artifactId") String artifactId,
            final @QueryParam("version") String version,
            final @QueryParam("classifier") String classifier,
            final @QueryParam("extension") String extension)
    throws UpdateServiceException {
        return new ConfiguredUpdateService(resolver,
                new ArtifactDescriptor.Builder()
                        .groupId(groupId)
                        .artifactId(artifactId)
                        .version(version)
                        .classifier(classifier)
                        .extension(extension)
                        .build());
    }
}

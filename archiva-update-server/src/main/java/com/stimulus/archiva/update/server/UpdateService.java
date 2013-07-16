/*
 * Copyright (C) 2005-2013 Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.server;

import java.util.Objects;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import static javax.ws.rs.core.MediaType.*;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Providers;

/**
 * Provides updates for *Archiva products.
 *
 * @author Christian Schlichtherle
 */
@Path("update")
@Produces({ APPLICATION_JSON, APPLICATION_XML, TEXT_XML })
public class UpdateService {

    private final java.nio.file.Path repository;

    /**
     * Constructs an update service.
     * This constructor immediately resolves the license consumer manager by
     * looking up a {@code ContextResolver<java.nio.file.Path>} in the
     * given providers.
     * You can provide a context resolver for this class like this:
     * <pre>{@code
     * package ...;
     *
     * import javax.ws.rs.ext.*;
     * import java.nio.file.Path;
     *
     * &#64;Provider
     * public class PathResolver
     * implements ContextResolver<java.nio.file.Path> {
     *     &#64;Override public java.nio.file.Path getContext(Class<?> type) {
     *         return ...;
     *     }
     * }
     * }</pre>
     *
     * @param providers the providers.
     */
    public UpdateService(@Context Providers providers) {
        this(path(providers));
    }

    private static java.nio.file.Path path(final Providers providers) {
        final ContextResolver<java.nio.file.Path>
                resolver = providers.getContextResolver(java.nio.file.Path.class, WILDCARD_TYPE);
        if (null == resolver)
            throw new IllegalArgumentException("No @Provider annotated ContextResolver<LicenseConsumerManager> available.");
        return resolver.getContext(java.nio.file.Path.class);
    }

    /**
     * Constructs an update service.
     * This is the preferable constructor with Dependency Injection frameworks,
     * e.g. CDI, Spring or Guice.
     *
     * @param repository the artifact repository.
     */
    @Inject
    public UpdateService(final java.nio.file.Path repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    java.nio.file.Path repository() { return repository; }

    @GET
    @Path("from/{groupId}/{artifactId}/{version}/{type}")
    public void from(
            final @PathParam("groupId") String groupId,
            final @PathParam("artifactId") String artifactId,
            final @PathParam("version") String version,
            final @PathParam("type") String type) {

    }
}

/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.jax.rs.server;

import java.util.Objects;
import java.util.concurrent.Callable;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import static javax.ws.rs.core.MediaType.*;
import javax.ws.rs.ext.*;
import net.java.trueupdate.artifact.ArtifactDescriptor;
import net.java.trueupdate.repository.spec.ArtifactRepository;
import net.java.trueupdate.jax.rs.UpdateServiceException;

/**
 * The (unconfigured) client-side implementation of an update service for
 * artifacts.
 *
 * @author Christian Schlichtherle
 */
@Path("update")
@Immutable
public final class UpdateServer {

    private final ArtifactRepository resolver;

    /**
     * Constructs an update service.
     * This constructor immediately resolves the artifact resolver by
     * looking up a {@code ContextResolver<ArtifactRepository>} in the
     * given providers.
     * You can provide a context resolver for this class like this:
     * <pre>{@code
     * package ...;
     *
     * import javax.ws.rs.ext.*;
     * import net.java.trueupdate.core.artifact.ArtifactRepository;
     *
     * &#64;Provider
     * public class ArtifactResolverResolver
     * implements ContextResolver<ArtifactRepository> {
     *     &#64;Override public ArtifactRepository getContext(Class<?> type) {
     *         return ...;
     *     }
     * }
     * }</pre>
     *
     * @param providers the providers.
     */
    public UpdateServer(@Context Providers providers) {
        this(resolver(providers));
    }

    private static ArtifactRepository resolver(final Providers providers) {
        final ContextResolver<ArtifactRepository>
                resolver = providers.getContextResolver(ArtifactRepository.class, WILDCARD_TYPE);
        if (null == resolver)
            throw new IllegalArgumentException("No @Provider annotated ContextResolver<ArtifactRepository> available.");
        return resolver.getContext(ArtifactRepository.class);
    }

    /**
     * Constructs an update service.
     * This is the preferable constructor for dependency injection.
     *
     * @param resolver the artifact resolver.
     */
    @Inject
    public UpdateServer(final ArtifactRepository resolver) {
        this.resolver = Objects.requireNonNull(resolver);
    }

    /** Returns a configured update service. */
    @Path("/")
    public ConfiguredUpdateServer configure(
            final @QueryParam("groupId") @Nullable String groupId,
            final @QueryParam("artifactId") @Nullable String artifactId,
            final @QueryParam("version") @Nullable  String version,
            final @QueryParam("classifier") @DefaultValue("") String classifier,
            final @QueryParam("extension") @DefaultValue("jar") String extension)
    throws UpdateServiceException {
        return UpdateServers.wrap(new Callable<ConfiguredUpdateServer>() {
            @Override
            public ConfiguredUpdateServer call() throws Exception {
                return new ConfiguredUpdateServer(resolver,
                        new ArtifactDescriptor.Builder()
                                .groupId(groupId)
                                .artifactId(artifactId)
                                .version(version)
                                .classifier(classifier)
                                .extension(extension)
                                .build());
            }
        });
    }
}

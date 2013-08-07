/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.jax.rs.client;

import com.sun.jersey.api.client.*;
import com.sun.jersey.api.client.ClientResponse.Status;
import java.io.*;
import java.net.URI;
import java.util.Objects;
import javax.annotation.CheckForNull;
import javax.annotation.concurrent.Immutable;
import javax.ws.rs.core.MediaType;
import static javax.ws.rs.core.MediaType.*;
import net.java.trueupdate.agent.spec.ArtifactDescriptor;
import net.java.trueupdate.core.io.Source;
import net.java.trueupdate.jax.rs.UpdateServiceException;
import static net.java.trueupdate.jax.rs.client.ArtifactDescriptors.queryParameters;

/**
 * RESTful web client based implementation of an update client.
 *
 * @author Christian Schlichtherle
 */
@Immutable
public final class ConfiguredUpdateClient implements UpdateClient {

    private final URI baseUri;
    private final Client client;

    public ConfiguredUpdateClient(URI baseUri) { this(baseUri, null); }

    public ConfiguredUpdateClient(final URI baseUri,
                                  final @CheckForNull Client client) {
        this.baseUri = Objects.requireNonNull(baseUri);
        this.client = null != client ? client : Client.create();
    }

    @Override
    public String version(ArtifactDescriptor descriptor) throws IOException {
        return version(descriptor, null);
    }

    public String version(ArtifactDescriptor descriptor,
                          @CheckForNull MediaType mediaType)
    throws IOException {
        return get(path("update/version")
                .queryParams(queryParameters(descriptor))
                .accept(null != mediaType ? mediaType : TEXT_PLAIN_TYPE)
        ).getEntity(String.class);
    }

    @Override public Source patch(
            final ArtifactDescriptor descriptor,
            final String updateVersion) {
        return new Source() {
            @Override public InputStream input() throws IOException {
                return get(path("update/patch")
                        .queryParams(queryParameters(descriptor))
                        .queryParam("update-version", updateVersion)
                        .accept(APPLICATION_OCTET_STREAM_TYPE)
                ).getEntityInputStream();
            }
        };
    }

    private WebResource path(String path) { return resource().path(path); }

    private WebResource resource() { return client.resource(baseUri); }

    private static ClientResponse get(WebResource.Builder builder)
    throws IOException {
        return checked(builder.get(ClientResponse.class));
    }

    private static ClientResponse checked(final ClientResponse response)
    throws IOException {
        final Status status = response.getClientResponseStatus();
        if (status != Status.OK)
            throw new UpdateServiceException(status.getStatusCode(),
                    new UniformInterfaceException(response));
        return response;
    }
}

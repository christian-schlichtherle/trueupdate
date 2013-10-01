/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.jaxrs.client;

import com.sun.jersey.api.client.*;
import com.sun.jersey.api.client.ClientResponse.Status;
import java.io.*;
import java.net.URI;
import javax.annotation.CheckForNull;
import javax.annotation.concurrent.Immutable;
import javax.ws.rs.core.MediaType;
import static javax.ws.rs.core.MediaType.*;
import net.java.trueupdate.artifact.spec.ArtifactDescriptor;
import net.java.trueupdate.core.io.Source;
import static net.java.trueupdate.jaxrs.client.ArtifactDescriptors.queryParameters;
import net.java.trueupdate.jaxrs.util.UpdateServiceException;
import static net.java.trueupdate.util.Objects.*;

/**
 * The client-side implementation of a RESTful service for artifact updates.
 *
 * @author Christian Schlichtherle
 */
@Immutable
public final class UpdateClient {

    private final URI uri;
    private final Client client;

    /**
     * Constructs an update client.
     *
     * @param uri the base URI of the web service.
     */
    public UpdateClient(URI uri) {
        this(uri, null);
    }

    /**
     * Constructs an artifact update client.
     *
     * @param uri the base URI of the web service.
     * @param client the nullable client.
     */
    public UpdateClient(final URI uri,
                        final @CheckForNull Client client) {
        this.uri = requireNonNull(uri);
        this.client = null != client ? client : Client.create();
    }

    /** Returns the base URI of the update service. */
    public URI uri() { return uri; }

    /**
     * Returns the update version for the described artifact.
     *
     * @param descriptor the artifact descriptor.
     * @return the update version for the described artifact.
     * @throws IOException on any I/O error, e.g. if the web service is not
     *         available.
     */
    public String version(ArtifactDescriptor descriptor) throws IOException {
        return version(descriptor, null);
    }

    public String version(ArtifactDescriptor descriptor,
                          @CheckForNull MediaType mediaType)
    throws IOException {
        return get(path("artifact/version")
                .queryParams(queryParameters(descriptor))
                .accept(null != mediaType ? mediaType : TEXT_PLAIN_TYPE)
        ).getEntity(String.class);
    }

    /**
     * Returns a source for reading the delta ZIP file for the described
     * artifact and its update version.
     * The web service is contacted whenever {@link Source#input()} is called,
     * so that any I/O exception may only be thrown from there.
     *
     * @param descriptor the artifact descriptor.
     * @param updateVersion the update version.
     * @return A source for reading the ZIP patch file for the described
     *         artifact and its update version.
     */
    public Source diff(final ArtifactDescriptor descriptor,
                       final String updateVersion) {
        return new Source() {
            @Override public InputStream input() throws IOException {
                return get(path("artifact/diff")
                        .queryParams(queryParameters(descriptor))
                        .queryParam("update-version", updateVersion)
                        .accept(APPLICATION_OCTET_STREAM_TYPE)
                ).getEntityInputStream();
            }
        };
    }

    private WebResource path(String path) { return resource().path(path); }

    private WebResource resource() { return client.resource(uri()); }

    private static ClientResponse get(WebResource.Builder builder)
    throws UpdateServiceException {
        return checked(builder.get(ClientResponse.class));
    }

    private static ClientResponse checked(final ClientResponse response)
    throws UpdateServiceException {
        final Status status = response.getClientResponseStatus();
        if (status != Status.OK)
            throw new UpdateServiceException(status.getStatusCode(),
                    new UniformInterfaceException(response));
        return response;
    }
}

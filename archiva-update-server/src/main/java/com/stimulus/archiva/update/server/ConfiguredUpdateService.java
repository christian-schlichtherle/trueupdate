package com.stimulus.archiva.update.server;

import com.stimulus.archiva.update.commons.ArtifactDescriptor;

import static java.util.Objects.requireNonNull;
import javax.annotation.concurrent.Immutable;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import static javax.ws.rs.core.MediaType.*;

/**
 * A configured update service for *Archiva products.
 *
 * @author Christian Schlichtherle
 */
@Produces({ APPLICATION_JSON, APPLICATION_XML, TEXT_XML })
@Immutable
public final class ConfiguredUpdateService {

    private static final QName VERSION_NAME = new QName("version");

    private final ArtifactResolver resolver;
    private final ArtifactDescriptor descriptor;

    /**
     * Constructs a configured update service with the given properties.
     *
     * @param resolver the artifact resolver.
     * @param descriptor the artifact descriptor for the client's current (!)
     *                   version.
     */
    public ConfiguredUpdateService(
            final ArtifactResolver resolver,
            final ArtifactDescriptor descriptor) {
        this.resolver = requireNonNull(resolver);
        this.descriptor = requireNonNull(descriptor);
    }

    @GET
    @Path("version")
    @Produces(APPLICATION_JSON)
    public String versionAsJson() throws UpdateServiceException {
        return '"' + versionAsText() + '"';
    }

    @GET
    @Path("version")
    @Produces({ APPLICATION_XML, TEXT_XML })
    public JAXBElement<String> versionAsXml() throws UpdateServiceException {
        return new JAXBElement<>(VERSION_NAME, String.class, versionAsText());
    }

    @GET
    @Path("version")
    @Produces(TEXT_PLAIN)
    public String versionAsText() throws UpdateServiceException {
        final ArtifactDescriptor update = resolveUpdateDescriptor();
        assert update.groupId().equals(descriptor.groupId());
        assert update.artifactId().equals(descriptor.artifactId());
        assert update.classifier().equals(descriptor.classifier());
        assert update.extension().equals(descriptor.extension());
        return descriptor.version();
    }

    private ArtifactDescriptor resolveUpdateDescriptor()
    throws UpdateServiceException {
        try {
            return resolver.resolveUpdateDescriptor(descriptor);
        } catch (Exception ex) {
            throw new UpdateServiceException(404, ex);
        }
    }
}

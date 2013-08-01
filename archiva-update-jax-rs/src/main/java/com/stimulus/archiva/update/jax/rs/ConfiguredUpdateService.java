/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.jax.rs;

import com.stimulus.archiva.update.core.artifact.*;
import com.stimulus.archiva.update.core.io.Sink;
import com.stimulus.archiva.update.core.io.Sinks;
import com.stimulus.archiva.update.core.zip.diff.ZipDiff;
import static com.stimulus.archiva.update.jax.rs.UpdateServices.wrap;
import java.io.*;
import static java.util.Objects.requireNonNull;
import java.util.concurrent.Callable;
import java.util.zip.ZipFile;
import javax.annotation.concurrent.Immutable;
import javax.ws.rs.*;
import static javax.ws.rs.core.MediaType.*;
import javax.ws.rs.core.StreamingOutput;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

/**
 * A configured update service for *Archiva products.
 *
 * @author Christian Schlichtherle
 */
@Immutable
public final class ConfiguredUpdateService {

    private static final QName VERSION_NAME = new QName("version");

    private final ArtifactResolver resolver;
    private final ArtifactDescriptor currentDescriptor;

    /**
     * Constructs a configured update service with the given properties.
     *
     * @param resolver the artifact resolver.
     * @param currentDescriptor the artifact descriptor for the client's
     *                          current version.
     */
    public ConfiguredUpdateService(
            final ArtifactResolver resolver,
            final ArtifactDescriptor currentDescriptor) {
        this.resolver = requireNonNull(resolver);
        this.currentDescriptor = requireNonNull(currentDescriptor);
    }

    @GET
    @Path("version")
    @Produces(TEXT_PLAIN)
    public String versionAsText() throws UpdateServiceException {
        return wrap(new Callable<String>() {
            @Override public String call() throws Exception {
                return resolveUpdateDescriptor().version();
            }
        });
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

    ArtifactDescriptor resolveUpdateDescriptor() throws Exception {
        return resolver.resolveUpdateDescriptor(currentDescriptor);
    }

    @GET
    @Path("patch")
    @Produces(APPLICATION_OCTET_STREAM)
    public StreamingOutput patch(
            final @QueryParam("update-version") String updateVersion)
    throws UpdateServiceException {
        return wrap(new Callable<StreamingOutput>() {
            @Override public StreamingOutput call() throws Exception {
                return streamingOutputWithZipPatchFile(
                        resolveArtifactFile(currentDescriptor),
                        resolveArtifactFile(updateDescriptor(updateVersion)));
            }
        });
    }

    ArtifactDescriptor updateDescriptor(String updateVersion) {
        return currentDescriptor.version(updateVersion);
    }

    File resolveArtifactFile(ArtifactDescriptor descriptor)
    throws Exception {
        return resolver.resolveArtifactFile(descriptor);
    }

    static StreamingOutput streamingOutputWithZipPatchFile(
            final File firstFile,
            final File secondFile) {
        return new StreamingOutput() {

            @Override
            public void write(final OutputStream output) throws IOException {
                write(Sinks.uncloseable(output));
            }

            void write(final Sink output) throws IOException {
                try (ZipFile firstZipFile = new ZipFile(firstFile);
                     ZipFile secondZipFile = new ZipFile(secondFile)) {
                    new ZipDiff.Builder()
                            .firstZipFile(firstZipFile)
                            .secondZipFile(secondZipFile)
                            .build()
                            .writeZipPatchFileTo(output);
                }
            }
        };
    }
}

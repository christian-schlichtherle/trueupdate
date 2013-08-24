/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.jax.rs.server;

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
import net.java.trueupdate.artifact.spec.ArtifactDescriptor;
import net.java.trueupdate.artifact.spec.ArtifactResolver;
import net.java.trueupdate.core.io.*;
import net.java.trueupdate.core.zip.diff.ZipDiff;
import static net.java.trueupdate.jax.rs.server.UpdateServers.wrap;
import net.java.trueupdate.jax.rs.util.UpdateServiceException;

/**
 * The configured server-side implementation of a RESTful service for
 * artifact updates.
 *
 * @author Christian Schlichtherle
 */
@Immutable
public final class ConfiguredUpdateServer {

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
    public ConfiguredUpdateServer(
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
                return resolveUpdateVersion();
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

    String resolveUpdateVersion() throws Exception {
        return resolver.resolveUpdateVersion(currentDescriptor);
    }

    @GET
    @Path("diff")
    @Produces(APPLICATION_OCTET_STREAM)
    public StreamingOutput diff(
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
            final File file1,
            final File file2) {
        return new StreamingOutput() {

            @Override
            public void write(final OutputStream output) throws IOException {
                write(Sinks.uncloseable(output));
            }

            void write(final Sink output) throws IOException {
                try (ZipFile zipFile1 = new ZipFile(file1);
                     ZipFile zipFile2 = new ZipFile(file2)) {
                    ZipDiff.builder()
                            .zipFile1(zipFile1)
                            .zipFile2(zipFile2)
                            .build()
                            .writeZipPatchFileTo(output);
                }
            }
        };
    }
}

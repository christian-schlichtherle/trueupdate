/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.jaxrs.server;

import java.io.*;
import java.util.concurrent.Callable;
import java.util.zip.ZipOutputStream;
import javax.annotation.WillNotClose;
import javax.annotation.concurrent.Immutable;
import javax.ws.rs.*;
import static javax.ws.rs.core.MediaType.*;
import javax.ws.rs.core.StreamingOutput;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import net.java.trueupdate.artifact.spec.ArtifactDescriptor;
import net.java.trueupdate.artifact.spec.ArtifactResolver;
import net.java.trueupdate.core.zip.ZipOutput;
import net.java.trueupdate.core.zip.ZipOutputStreamAdapter;
import net.java.trueupdate.core.zip.ZipSink;
import net.java.trueupdate.core.zip.diff.ZipDiff;
import static net.java.trueupdate.jaxrs.server.UpdateServers.wrap;
import net.java.trueupdate.jaxrs.util.UpdateServiceException;

/**
 * The configured server-side implementation of a RESTful service for
 * artifact updates.
 * Instances of this class can only get obtained by calling the method
 * {@link BasicUpdateServer#artifact}.
 *
 * @author Christian Schlichtherle
 */
@Immutable
public final class ConfiguredUpdateServer {

    private static final QName VERSION_NAME = new QName("version");

    private final ArtifactResolver resolver;
    private final ArtifactDescriptor currentDescriptor;

    ConfiguredUpdateServer(
            final ArtifactResolver resolver,
            final ArtifactDescriptor currentDescriptor) {
        assert null != resolver;
        this.resolver = resolver;
        assert null != currentDescriptor;
        this.currentDescriptor = currentDescriptor;
    }

    @GET
    @Path("version")
    @Produces({ APPLICATION_XML, TEXT_XML })
    public JAXBElement<String> versionAsXml() throws UpdateServiceException {
        return new JAXBElement<String>(VERSION_NAME, String.class, versionAsText());
    }

    @GET
    @Path("version")
    @Produces(APPLICATION_JSON)
    public String versionAsJson() throws UpdateServiceException {
        return '"' + versionAsText() + '"';
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
                return diff(
                        resolveArtifactFile(currentDescriptor),
                        resolveArtifactFile(updateDescriptor(updateVersion)));
            }
        });
    }

    ArtifactDescriptor updateDescriptor(String updateVersion) {
        return currentDescriptor.version(updateVersion);
    }

    File resolveArtifactFile(ArtifactDescriptor descriptor) throws Exception {
        return resolver.resolveArtifactFile(descriptor);
    }

    static StreamingOutput diff(final File input1, final File input2) {
        return new StreamingOutput() {
            @Override public void write(final @WillNotClose OutputStream out)
            throws IOException {
                class DiffSink implements ZipSink {
                    @Override public ZipOutput output() throws IOException {
                        return new ZipOutputStreamAdapter(new ZipOutputStream(out) {
                            @Override public void close() throws IOException {
                                super.finish();
                            }
                        });
                    }
                } // DiffSink
                ZipDiff .builder()
                        .input1(input1)
                        .input2(input2)
                        .build()
                        .output(new DiffSink());
            }
        };
    }
}

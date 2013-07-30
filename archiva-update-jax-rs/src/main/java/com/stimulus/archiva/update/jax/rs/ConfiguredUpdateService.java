/*
 * Copyright (C) 2005-2013 Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.jax.rs;

import com.stimulus.archiva.update.core.artifact.*;
import com.stimulus.archiva.update.core.io.MemoryStore;
import com.stimulus.archiva.update.core.zip.diff.ZipDiff;
import java.io.*;
import java.util.concurrent.Callable;
import java.util.zip.ZipFile;
import static java.util.Objects.requireNonNull;
import javax.annotation.concurrent.Immutable;
import javax.ws.rs.*;
import static javax.ws.rs.core.MediaType.*;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

/**
 * A configured update service for *Archiva products.
 *
 * @author Christian Schlichtherle
 */
@Produces({ APPLICATION_JSON, APPLICATION_XML, TEXT_XML })
@Immutable
public final class ConfiguredUpdateService {

    private static final QName VERSION_NAME = new QName("version");

    private static final int NOT_FOUND = 404;

    private final ArtifactResolver resolver;
    private final ArtifactDescriptor currentDescriptor;

    /**
     * Constructs a configured update service with the given properties.
     *
     * @param resolver the artifact resolver.
     * @param currentDescriptor the artifact descriptor for the client's current (!)
     *                   version.
     */
    public ConfiguredUpdateService(
            final ArtifactResolver resolver,
            final ArtifactDescriptor currentDescriptor) {
        this.resolver = requireNonNull(resolver);
        this.currentDescriptor = requireNonNull(currentDescriptor);
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
        return wrap(new Callable<String>() {
            @Override public String call() throws Exception {
                return resolveUpdateDescriptor().version();
            }
        });
    }

    @GET
    @Path("patch")
    @Produces(APPLICATION_OCTET_STREAM)
    public InputStream patch() throws UpdateServiceException {
        return wrap(new Callable<InputStream>() {
            @Override public InputStream call() throws Exception {
                final ArtifactDescriptor
                        updateDescriptor = resolveUpdateDescriptor();
                if (currentDescriptor.version().equals(updateDescriptor.version()))
                    throw new AlreadyUpToDateException(currentDescriptor);
                final File currentFile = resolveArtifactFile(currentDescriptor);
                final File updateFile = resolveArtifactFile(updateDescriptor);
                try (ZipFile currentZip = new ZipFile(currentFile);
                     ZipFile updateZip = new ZipFile(updateFile)) {
                    final MemoryStore store = new MemoryStore();
                    new ZipDiff.Builder()
                            .firstZipFile(currentZip)
                            .secondZipFile(updateZip)
                            .build()
                            .writeDiffFileTo(store);
                    return store.input();
                }
            }
        });
    }

    private ArtifactDescriptor resolveUpdateDescriptor() throws Exception {
        final ArtifactDescriptor updateDescriptor =
                resolver.resolveUpdateDescriptor(currentDescriptor);
        assert updateDescriptor.groupId().equals(currentDescriptor.groupId());
        assert updateDescriptor.artifactId().equals(currentDescriptor.artifactId());
        assert updateDescriptor.classifier().equals(currentDescriptor.classifier());
        assert updateDescriptor.extension().equals(currentDescriptor.extension());
        return updateDescriptor;
    }

    private File resolveArtifactFile(ArtifactDescriptor descriptor)
    throws Exception {
        return resolver.resolveArtifactFile(descriptor);
    }

    private static <V> V wrap(final Callable<V> task)
    throws UpdateServiceException {
        try {
            return task.call();
        } catch (RuntimeException | UpdateServiceException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new UpdateServiceException(NOT_FOUND, ex);
        }
    }
}

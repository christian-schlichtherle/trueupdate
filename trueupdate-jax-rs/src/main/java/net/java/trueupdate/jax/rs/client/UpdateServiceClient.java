/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.jax.rs.client;

import net.java.trueupdate.core.artifact.ArtifactDescriptor;
import net.java.trueupdate.core.io.Source;

import java.io.IOException;

/**
 * The client-side interface of an update service for web apps.
 *
 * @author Christian Schlichtherle
 */
public interface UpdateServiceClient {

    /**
     * Returns the update version for the described artifact.
     *
     * @param descriptor the artifact descriptor.
     * @return the update version for the described artifact.
     * @throws IOException on any I/O error, e.g. if the web service is not
     *         available.
     */
    String version(ArtifactDescriptor descriptor) throws IOException;

    /**
     * Returns a source for reading the ZIP patch file for the described
     * artifact and its update version.
     * The web service is contacted whenever {@link Source#input()} is called,
     * so that any I/O exception will be postponed until that method gets
     * called.
     *
     * @param descriptor the artifact descriptor.
     * @param updateVersion the update version.
     * @return A source for reading the ZIP patch file for the described
     *         artifact and its update version.
     */
    Source patch(ArtifactDescriptor descriptor, String updateVersion);
}

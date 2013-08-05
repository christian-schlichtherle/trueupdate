/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.client;

import com.stimulus.archiva.update.core.artifact.ArtifactDescriptor;
import javax.annotation.CheckForNull;

/**
 * Indicates that the current version of an artifact is already up-to-date,
 * that is that there is no later version available.
 *
 * @author Christian Schlichtherle
 */
public final class AlreadyUpToDateException extends Exception {

    private static final long serialVersionUID = 0L;

    private final @CheckForNull ArtifactDescriptor descriptor;

    AlreadyUpToDateException(final @CheckForNull ArtifactDescriptor descriptor) {
        super(descriptor.toString());
        this.descriptor = descriptor;
    }

    public ArtifactDescriptor descriptor() { return descriptor; }
}

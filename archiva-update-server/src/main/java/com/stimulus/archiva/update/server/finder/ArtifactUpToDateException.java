package com.stimulus.archiva.update.server.finder;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;

/**
 * Indicates that a described artifact is already up-to-date.
 *
 * @author Christian Schlichtherle
 */
public final class ArtifactUpToDateException extends Exception {

    private static final long serialVersionUID = 0L;

    private final @Nullable ArtifactDescriptor descriptor;

    /** Constructs an up-to-date-exception for the described artifact. */
    public ArtifactUpToDateException(final @CheckForNull ArtifactDescriptor descriptor) {
        this.descriptor = descriptor;
    }

    @Override public String getMessage() {
        return "The artifact "
                + (null != descriptor ? descriptor + " " : "")
                + "is already up-to-date.";
    }
}

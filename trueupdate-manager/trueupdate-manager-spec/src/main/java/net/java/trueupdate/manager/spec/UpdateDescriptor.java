/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.spec;

import java.io.Serializable;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import net.java.trueupdate.artifact.spec.ArtifactDescriptor;
import static net.java.trueupdate.util.Objects.*;
import static net.java.trueupdate.util.Strings.*;

/**
 * An update descriptor comprises of an artifact descriptor plus an update
 * version.
 * This class implements an immutable value object, so you can easily share it
 * with anyone.
 *
 * @author Christian Schlichtherle
 */
@Immutable
public final class UpdateDescriptor implements Serializable {

    private static final long serialVersionUID = 0L;

    private final ArtifactDescriptor artifactDescriptor;
    private final String updateVersion;

    UpdateDescriptor(final Builder<?> b) {
        this.artifactDescriptor = requireNonNull(b.artifactDescriptor);
        this.updateVersion = requireNonEmpty(b.updateVersion);
    }

    /** Returns a new builder with all properties set from this instance. */
    public Builder<Void> update() {
        return builder()
                .artifactDescriptor(artifactDescriptor())
                .updateVersion(updateVersion());
    }

    /** Returns a new builder for an update descriptor. */
    public static Builder<Void> builder() { return new Builder<Void>(); }

    /** Returns the artifact descriptor. */
    public ArtifactDescriptor artifactDescriptor() {
        return artifactDescriptor;
    }

    /** Returns an update descriptor with the given artifact descriptor. */
    public UpdateDescriptor artifactDescriptor(ArtifactDescriptor artifactDescriptor) {
        return artifactDescriptor().equals(artifactDescriptor)
                ? this
                : update().artifactDescriptor(artifactDescriptor).build();
    }

    /** Returns the update version. */
    public String updateVersion() { return updateVersion; }

    /** Returns an update descriptor with the given update version. */
    public UpdateDescriptor updateVersion(String updateVersion) {
        return updateVersion().equals(updateVersion)
                ? this
                : update().updateVersion(updateVersion).build();
    }

    @Override public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof UpdateDescriptor)) return false;
        final UpdateDescriptor that = (UpdateDescriptor) obj;
        return  this.artifactDescriptor().equals(that.artifactDescriptor()) &&
                this.updateVersion().equals(that.updateVersion());
    }

    @Override public int hashCode() {
        int hash = 17;
        hash = 31 * hash + artifactDescriptor().hashCode();
        hash = 31 * hash + updateVersion.hashCode();
        return hash;
    }

    /**
     * A builder for an update descriptor.
     *
     * @param <P> The type of the parent builder.
     */
    @SuppressWarnings("PackageVisibleField")
    public static class Builder<P> {

        @CheckForNull ArtifactDescriptor artifactDescriptor;
        @CheckForNull String updateVersion;

        protected Builder() { }

        public ArtifactDescriptor.Builder<Builder<P>> artifactDescriptor() {
            return new ArtifactDescriptor.Builder<Builder<P>>() {
                @Override public Builder<P> inject() {
                    return artifactDescriptor(build());
                }
            };
        }

        public Builder<P> artifactDescriptor(
                final @Nullable ArtifactDescriptor artifactDescriptor) {
            this.artifactDescriptor = artifactDescriptor;
            return this;
        }

        public Builder<P> updateVersion(final @Nullable String updateVersion) {
            this.updateVersion = updateVersion;
            return this;
        }

        public UpdateDescriptor build() {
            return new UpdateDescriptor(this);
        }

        /**
         * Injects the product of this builder into the parent builder, if
         * defined.
         *
         * @throws IllegalStateException if there is no parent builder defined.
         */
        public P inject() {
            throw new IllegalStateException("No parent builder defined.");
        }
    } // Builder
}

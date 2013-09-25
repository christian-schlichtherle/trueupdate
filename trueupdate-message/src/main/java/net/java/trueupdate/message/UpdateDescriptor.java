/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.message;

import javax.annotation.*;
import javax.annotation.concurrent.Immutable;
import net.java.trueupdate.artifact.spec.ArtifactDescriptor;
import static net.java.trueupdate.util.Objects.*;
import static net.java.trueupdate.util.Strings.*;
import net.java.trueupdate.util.builder.AbstractBuilder;

/**
 * An update descriptor comprises of an artifact descriptor and an update
 * version.
 * This class implements an immutable value object, so you can easily share its
 * instances with anyone or use them as map keys.
 *
 * @author Christian Schlichtherle
 */
@Immutable
public final class UpdateDescriptor {

    private final ArtifactDescriptor artifactDescriptor;
    private final String updateVersion;

    UpdateDescriptor(final Builder<?> b) {
        this.artifactDescriptor = requireNonNull(b.artifactDescriptor);
        this.updateVersion = requireNonEmpty(b.updateVersion);
    }

    /** Returns a new builder for an update descriptor. */
    public static Builder<Void> builder() { return new Builder<Void>(); }

    /** Returns the artifact descriptor. */
    public ArtifactDescriptor artifactDescriptor() {
        return artifactDescriptor;
    }

    /** Returns the update version. */
    public String updateVersion() { return updateVersion; }

    @Override@SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof UpdateDescriptor)) return false;
        final UpdateDescriptor that = (UpdateDescriptor) obj;
        return  this.artifactDescriptor.equals(that.artifactDescriptor) &&
                this.updateVersion.equals(that.updateVersion);
    }

    @Override public int hashCode() {
        int hash = 17;
        hash = 31 * hash + artifactDescriptor.hashCode();
        hash = 31 * hash + updateVersion.hashCode();
        return hash;
    }

    /**
     * A builder for an update descriptor.
     *
     * @param <P> The type of the parent builder, if defined.
     */
    @SuppressWarnings("PackageVisibleField")
    public static class Builder<P> extends AbstractBuilder<P> {

        @CheckForNull ArtifactDescriptor artifactDescriptor;
        @CheckForNull String updateVersion;

        protected Builder() { }

        public final ArtifactDescriptor.Builder<Builder<P>> artifactDescriptor() {
            return new ArtifactDescriptor.Builder<Builder<P>>() {
                @Override public Builder<P> inject() {
                    return artifactDescriptor(build());
                }
            };
        }

        public final Builder<P> artifactDescriptor(
                final @Nullable ArtifactDescriptor artifactDescriptor) {
            this.artifactDescriptor = artifactDescriptor;
            return this;
        }

        public final Builder<P> updateVersion(
                final @Nullable String updateVersion) {
            this.updateVersion = updateVersion;
            return this;
        }

        @Override public final UpdateDescriptor build() {
            return new UpdateDescriptor(this);
        }
    } // Builder
}

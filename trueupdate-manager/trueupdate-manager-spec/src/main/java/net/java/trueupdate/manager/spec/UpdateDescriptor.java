/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.spec;

import net.java.trueupdate.artifact.spec.ArtifactDescriptor;

import static java.util.Objects.requireNonNull;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * An update descriptor comprises of an artifact descriptor plus an update
 * version.
 * This class implements an immutable value object, so you can easily share it
 * with anyone.
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

    private static String requireNonEmpty(final String value) {
        if (value.isEmpty()) throw new IllegalArgumentException();
        return value;
    }

    /** Returns a new builder with all properties set from this instance. */
    public Builder<Void> update() {
        return builder()
                .artifactDescriptor(artifactDescriptor())
                .updateVersion(updateVersion());
    }

    /** Returns a new builder for an update descriptor. */
    public static Builder<Void> builder() { return new Builder<>(); }

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

    /** A builder for an update descriptor. */
    @SuppressWarnings(value = "PackageVisibleField")
    public static class Builder<T> {

        @CheckForNull ArtifactDescriptor artifactDescriptor;
        @CheckForNull String updateVersion;

        protected Builder() { }

        public ArtifactDescriptor.Builder<Builder<T>> artifactDescriptor() {
            return new ArtifactDescriptor.Builder<Builder<T>>() {
                @Override public Builder<T> inject() {
                    return artifactDescriptor(build());
                }
            };
        }

        public Builder<T> artifactDescriptor(
                final @Nullable ArtifactDescriptor artifactDescriptor) {
            this.artifactDescriptor = artifactDescriptor;
            return this;
        }

        public Builder<T> updateVersion(final @Nullable String updateVersion) {
            this.updateVersion = updateVersion;
            return this;
        }

        public UpdateDescriptor build() {
            return new UpdateDescriptor(this);
        }

        public T inject() {
            throw new IllegalStateException("No target for injection.");
        }
    } // Builder
}

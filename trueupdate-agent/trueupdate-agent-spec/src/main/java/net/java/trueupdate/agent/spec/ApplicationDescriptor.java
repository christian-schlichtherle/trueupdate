/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.agent.spec;

import java.net.URI;
import static java.util.Objects.requireNonNull;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import net.java.trueupdate.artifact.spec.ArtifactDescriptor;

/**
 * A Value Object which describes an installed application.
 *
 * @author Christian Schlichtherle
 */
public final class ApplicationDescriptor {

    private final ArtifactDescriptor artifactDescriptor;
    private final URI currentLocation;

    ApplicationDescriptor(final Builder<?> b) {
        this.artifactDescriptor = requireNonNull(b.artifactDescriptor);
        this.currentLocation = requireNonNull(b.currentLocation);
    }

    /** Returns a new builder for an application descriptor. */
    public static Builder<Void> builder() { return new Builder<>(); }

    /** Returns the artifact descriptor. */
    public ArtifactDescriptor artifactDescriptor() {
        return artifactDescriptor;
    }

    /** Returns the current location. */
    public URI currentLocation() { return currentLocation; }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ApplicationDescriptor)) return false;
        final ApplicationDescriptor that = (ApplicationDescriptor) obj;
        return  this.artifactDescriptor().equals(that.artifactDescriptor()) &&
                this.currentLocation().equals(that.currentLocation());
    }

    @Override
    public int hashCode() {
        int hash = 17;
        hash = 31 * hash + artifactDescriptor().hashCode();
        hash = 31 * hash + currentLocation().hashCode();
        return hash;
    }

    @SuppressWarnings(value = "PackageVisibleField")
    public static class Builder<T> {

        @CheckForNull ArtifactDescriptor artifactDescriptor;
        @CheckForNull URI currentLocation;

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

        public Builder<T> currentLocation(final @Nullable URI currentLocation) {
            this.currentLocation = currentLocation;
            return this;
        }

        public ApplicationDescriptor build() {
            return new ApplicationDescriptor(this);
        }

        public T inject() {
            throw new IllegalStateException("No target for injection.");
        }
    } // Builder
}

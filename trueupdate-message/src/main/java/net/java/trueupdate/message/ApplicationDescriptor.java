/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.message;

import javax.annotation.*;
import javax.annotation.concurrent.Immutable;
import net.java.trueupdate.artifact.spec.ArtifactDescriptor;
import static net.java.trueupdate.util.Objects.requireNonNull;
import static net.java.trueupdate.util.Strings.requireNonEmpty;
import net.java.trueupdate.util.builder.AbstractBuilder;

/**
 * An application descriptor comprises of an artifact descriptor and a current
 * location.
 * This class implements an immutable value object, so you can easily share its
 * instances with anyone or use them as map keys.
 *
 * @author Christian Schlichtherle
 */
@Immutable
public final class ApplicationDescriptor {

    private final ArtifactDescriptor artifactDescriptor;
    private final String currentLocation;

    ApplicationDescriptor(final Builder<?> b) {
        this.artifactDescriptor = requireNonNull(b.artifactDescriptor);
        this.currentLocation = requireNonEmpty(b.currentLocation);
    }

    /** Returns a new builder for an application descriptor. */
    public static Builder<Void> builder() { return new Builder<Void>(); }

    /** Returns the artifact descriptor. */
    public ArtifactDescriptor artifactDescriptor() {
        return artifactDescriptor;
    }

    /** Returns the current location. */
    public String currentLocation() { return currentLocation; }

    @Override
    @SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
    public boolean equals(final Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ApplicationDescriptor)) return false;
        final ApplicationDescriptor that = (ApplicationDescriptor) obj;
        return  this.artifactDescriptor.equals(that.artifactDescriptor) &&
                this.currentLocation.equals(that.currentLocation);
    }

    @Override
    public int hashCode() {
        int hash = 17;
        hash = 31 * hash + artifactDescriptor.hashCode();
        hash = 31 * hash + currentLocation.hashCode();
        return hash;
    }

    /**
     * A builder for an application descriptor.
     *
     * @param <P> The type of the parent builder, if defined.
     */
    @SuppressWarnings("PackageVisibleField")
    public static class Builder<P> extends AbstractBuilder<P> {

        @CheckForNull ArtifactDescriptor artifactDescriptor;
        @CheckForNull String currentLocation;

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

        public final Builder<P> currentLocation(
                final @Nullable String currentLocation) {
            this.currentLocation = currentLocation;
            return this;
        }

        @Override public final ApplicationDescriptor build() {
            return new ApplicationDescriptor(this);
        }
    } // Builder
}

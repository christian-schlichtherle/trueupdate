/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.agent.spec;

import javax.annotation.*;
import javax.annotation.concurrent.Immutable;
import net.java.trueupdate.agent.spec.dto.ApplicationParametersDto;
import net.java.trueupdate.artifact.spec.ArtifactDescriptor;
import net.java.trueupdate.manager.spec.ApplicationDescriptor;
import static net.java.trueupdate.util.Objects.requireNonNull;
import static net.java.trueupdate.util.Strings.*;
import static net.java.trueupdate.util.SystemProperties.resolve;

/**
 * Application Parameters.
 *
 * @author Christian Schlichtherle
 */
@Immutable
@SuppressWarnings("rawtypes")
public final class ApplicationParameters {

    private final UpdateAgentListener updateAgentListener;
    private final ArtifactDescriptor artifactDescriptor;
    private final String currentLocation, updateLocation;

    ApplicationParameters(final Builder<?> b) {
        this.updateAgentListener = requireNonNull(b.updateAgentListener);
        this.artifactDescriptor = requireNonNull(b.artifactDescriptor);
        this.currentLocation = requireNonEmpty(b.currentLocation);
        this.updateLocation = nonEmptyOr(b.updateLocation, currentLocation);
    }

    /** Parses the given configuration item. */
    public static ApplicationParameters parse(ApplicationParametersDto ci) {
        return builder().parse(ci).build();
    }

    /**
     * Returns a new builder for application parameters.
     * The default value for the property {@code updateLocation} is the
     * value of the property {@code currentLocation}.
     */
    public static Builder<Void> builder() { return new Builder<Void>(); }

    /** Returns the update agent listener. */
    public UpdateAgentListener updateAgentListener() {
        return updateAgentListener;
    }

    /** Returns the artifact descriptor. */
    public ArtifactDescriptor artifactDescriptor() {
        return artifactDescriptor;
    }

    /** Returns the current location. */
    public String currentLocation() { return currentLocation; }

    /** Returns the update location. */
    public String updateLocation() { return updateLocation; }

    /** Vends an application descriptor from these application parameters. */
    public ApplicationDescriptor applicationDescriptor() {
        return ApplicationDescriptor
                .builder()
                .artifactDescriptor(artifactDescriptor)
                .currentLocation(currentLocation)
                .build();
    }

    /**
     * A builder for application parameters.
     *
     * @param <P> The type of the parent builder.
     */
    @SuppressWarnings("PackageVisibleField")
    public static class Builder<P> {

        @CheckForNull UpdateAgentListener updateAgentListener;
        @CheckForNull ArtifactDescriptor artifactDescriptor;
        @CheckForNull String currentLocation, updateLocation;

        protected Builder() { }

        /** Parses the given configuration item. */
        public Builder<P> parse(final ApplicationParametersDto ci) {
            if (null != ci.listenerClass)
                updateAgentListener = listener(ci.listenerClass);
            if (null != ci.artifact)
                artifactDescriptor = ArtifactDescriptor.parse(ci.artifact);
            currentLocation = resolve(ci.currentLocation, currentLocation);
            updateLocation = resolve(ci.updateLocation, updateLocation);
            return this;
        }

        private static UpdateAgentListener listener(final String className) {
            try {
                return (UpdateAgentListener) Thread
                        .currentThread()
                        .getContextClassLoader()
                        .loadClass(resolve(className))
                        .newInstance();
            } catch (Exception ex) {
                throw new IllegalArgumentException(ex);
            }
        }

        public Builder<P> updateAgentListener(
                final @Nullable UpdateAgentListener listener) {
            this.updateAgentListener = listener;
            return this;
        }

        public ArtifactDescriptor.Builder<Builder<P>> artifactDescriptor() {
            return new ArtifactDescriptor.Builder<Builder<P>>() {
                @Override public Builder<P> inject() {
                    return artifactDescriptor(build());
                }
            };
        }

        public Builder<P> artifactDescriptor(
                final @Nullable ArtifactDescriptor descriptor) {
            this.artifactDescriptor = descriptor;
            return this;
        }

        public Builder<P> currentLocation(
                final @Nullable String location) {
            this.currentLocation = location;
            return this;
        }

        public Builder<P> updateLocation(
                final @Nullable String location) {
            this.updateLocation = location;
            return this;
        }

        public ApplicationParameters build() {
            return new ApplicationParameters(this);
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

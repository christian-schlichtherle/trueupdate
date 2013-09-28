/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.agent.core;

import javax.annotation.*;
import javax.annotation.concurrent.Immutable;
import net.java.trueupdate.agent.spec.UpdateAgentListener;
import net.java.trueupdate.agent.core.dto.ApplicationParametersDto;
import net.java.trueupdate.artifact.spec.ArtifactDescriptor;
import static net.java.trueupdate.util.Objects.*;
import static net.java.trueupdate.util.Strings.*;
import static net.java.trueupdate.util.SystemProperties.resolve;
import net.java.trueupdate.util.builder.AbstractBuilder;

/**
 * Application Parameters.
 *
 * @author Christian Schlichtherle
 */
@Immutable
@SuppressWarnings("rawtypes")
public final class ApplicationParameters {

    private final ArtifactDescriptor artifactDescriptor;
    private final String currentLocation, updateLocation;
    private final UpdateAgentListener updateAgentListener;

    ApplicationParameters(final Builder<?> b) {
        this.artifactDescriptor = requireNonNull(b.artifactDescriptor);
        this.currentLocation = requireNonEmpty(b.currentLocation);
        this.updateLocation = nonEmptyOr(b.updateLocation, currentLocation);
        this.updateAgentListener = null != b.listenerClass
                ? listener(b.listenerClass)
                : new UpdateAgentListener();
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

    /**
     * A builder for application parameters.
     *
     * @param <P> The type of the parent builder, if defined.
     */
    @SuppressWarnings("PackageVisibleField")
    public static class Builder<P> extends AbstractBuilder<P> {

        @CheckForNull ArtifactDescriptor artifactDescriptor;
        @CheckForNull String currentLocation, updateLocation, listenerClass;

        protected Builder() { }

        /** Parses the given configuration item. */
        public final Builder<P> parse(final ApplicationParametersDto ci) {
            if (null != ci.artifact)
                artifactDescriptor = ArtifactDescriptor.parse(ci.artifact);
            currentLocation = resolve(ci.currentLocation, currentLocation);
            updateLocation = resolve(ci.updateLocation, updateLocation);
            listenerClass = resolve(ci.listenerClass, listenerClass);
            return this;
        }

        public final ArtifactDescriptor.Builder<Builder<P>> artifactDescriptor() {
            return new ArtifactDescriptor.Builder<Builder<P>>() {
                @Override public Builder<P> inject() {
                    return artifactDescriptor(build());
                }
            };
        }

        public final Builder<P> artifactDescriptor(
                final @Nullable ArtifactDescriptor descriptor) {
            this.artifactDescriptor = descriptor;
            return this;
        }

        public final Builder<P> currentLocation(
                final @Nullable String location) {
            this.currentLocation = location;
            return this;
        }

        public final Builder<P> updateLocation(
                final @Nullable String location) {
            this.updateLocation = location;
            return this;
        }

        public final Builder<P> listenerClass(final @Nullable String listenerClass) {
            this.listenerClass = listenerClass;
            return this;
        }

        @Override public final ApplicationParameters build() {
            return new ApplicationParameters(this);
        }
    } // Builder
}

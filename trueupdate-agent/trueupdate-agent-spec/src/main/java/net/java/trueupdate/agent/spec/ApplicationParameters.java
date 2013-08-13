/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.agent.spec;

import java.net.URI;
import static java.util.Objects.requireNonNull;
import javax.annotation.*;
import net.java.trueupdate.manager.spec.ApplicationDescriptor;

/**
 * Application Parameters.
 *
 * @author Christian Schlichtherle
 */
public final class ApplicationParameters {

    private final ApplicationListener applicationListener;
    private final ApplicationDescriptor applicationDescriptor;
    private final URI updateLocation;

    ApplicationParameters(final Builder<?> b) {
        this.applicationListener = requireNonNull(b.applicationListener);
        this.applicationDescriptor = requireNonNull(b.applicationDescriptor);
        this.updateLocation = nonNullOr(b.updateLocation,
                applicationDescriptor.currentLocation());
    }

    private static <T> T nonNullOr(T value, T eagerDefault) {
        return null != value ? value : eagerDefault;
    }

    /**
     * Returns a new builder for update agent parameters.
     * The default value for the property {@code updateLocation} is the
     * current location as found in the application descriptor.
     */
    public static Builder<Void> builder() { return new Builder<>(); }

    public ApplicationListener applicationListener() {
        return applicationListener;
    }

    public ApplicationDescriptor applicationDescriptor() {
        return applicationDescriptor;
    }

    public URI updateLocation() { return updateLocation; }

    @SuppressWarnings(value = "PackageVisibleField")
    public static class Builder<T> {

        @CheckForNull ApplicationListener applicationListener;
        @CheckForNull ApplicationDescriptor applicationDescriptor;
        @CheckForNull URI updateLocation;

        protected Builder() { }

        public ApplicationDescriptor.Builder<Builder<T>> applicationDescriptor() {
            return new ApplicationDescriptor.Builder<Builder<T>>() {
                @Override
                public Builder<T> inject() {
                    return updateAgentDescriptor(build());
                }
            };
        }

        public Builder<T> applicationListener(
                final @Nullable ApplicationListener applicationListener) {
            this.applicationListener = applicationListener;
            return this;
        }

        public Builder<T> updateAgentDescriptor(
                final @Nullable ApplicationDescriptor descriptor) {
            this.applicationDescriptor = descriptor;
            return this;
        }

        public Builder<T> updateLocation(
                final @Nullable URI updateLocation) {
            this.updateLocation = updateLocation;
            return this;
        }

        public ApplicationParameters build() {
            return new ApplicationParameters(this);
        }

        public T inject() {
            throw new IllegalStateException("No target for injection.");
        }
    } // Builder

} // Parameters
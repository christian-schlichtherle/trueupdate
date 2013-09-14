/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.agent.spec;

import javax.annotation.*;
import javax.annotation.concurrent.Immutable;
import net.java.trueupdate.manager.spec.ApplicationDescriptor;
import static net.java.trueupdate.util.Objects.requireNonNull;
import static net.java.trueupdate.util.Strings.nonEmptyOr;

/**
 * Application Parameters.
 *
 * @author Christian Schlichtherle
 */
@Immutable
public final class ApplicationParameters {

    private final ApplicationListener applicationListener;
    private final ApplicationDescriptor applicationDescriptor;
    private final String updateLocation;

    ApplicationParameters(final Builder<?> b) {
        this.applicationListener = requireNonNull(b.applicationListener);
        this.applicationDescriptor = requireNonNull(b.applicationDescriptor);
        this.updateLocation = nonEmptyOr(b.updateLocation,
                applicationDescriptor.currentLocation());
    }

    /**
     * Returns a new builder for application parameters.
     * The default value for the property {@code updateLocation} is the
     * current location as found in the application descriptor.
     */
    public static Builder<Void> builder() { return new Builder<Void>(); }

    public ApplicationListener applicationListener() {
        return applicationListener;
    }

    public ApplicationDescriptor applicationDescriptor() {
        return applicationDescriptor;
    }

    public String updateLocation() { return updateLocation; }

    @SuppressWarnings(value = "PackageVisibleField")
    public static class Builder<T> {

        @CheckForNull ApplicationListener applicationListener;
        @CheckForNull ApplicationDescriptor applicationDescriptor;
        @CheckForNull String updateLocation;

        protected Builder() { }

        public ApplicationDescriptor.Builder<Builder<T>> applicationDescriptor() {
            return new ApplicationDescriptor.Builder<Builder<T>>() {
                @Override public Builder<T> inject() {
                    return applicationDescriptor(build());
                }
            };
        }

        public Builder<T> applicationListener(
                final @Nullable ApplicationListener applicationListener) {
            this.applicationListener = applicationListener;
            return this;
        }

        public Builder<T> applicationDescriptor(
                final @Nullable ApplicationDescriptor applicationDescriptor) {
            this.applicationDescriptor = applicationDescriptor;
            return this;
        }

        public Builder<T> updateLocation(
                final @Nullable String updateLocation) {
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
}

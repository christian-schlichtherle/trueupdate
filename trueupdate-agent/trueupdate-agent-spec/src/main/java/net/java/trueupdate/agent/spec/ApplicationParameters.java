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
@SuppressWarnings("rawtypes")
public final class ApplicationParameters {

    private final UpdateAgentListener applicationListener;
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

    public UpdateAgentListener applicationListener() {
        return applicationListener;
    }

    public ApplicationDescriptor applicationDescriptor() {
        return applicationDescriptor;
    }

    public String updateLocation() { return updateLocation; }

    /**
     * A builder for application parameters.
     *
     * @param <P> The type of the parent builder.
     */
    @SuppressWarnings("PackageVisibleField")
    public static class Builder<P> {

        @CheckForNull UpdateAgentListener applicationListener;
        @CheckForNull ApplicationDescriptor applicationDescriptor;
        @CheckForNull String updateLocation;

        protected Builder() { }

        public ApplicationDescriptor.Builder<Builder<P>> applicationDescriptor() {
            return new ApplicationDescriptor.Builder<Builder<P>>() {
                @Override public Builder<P> inject() {
                    return applicationDescriptor(build());
                }
            };
        }

        public Builder<P> applicationListener(
                final @Nullable UpdateAgentListener applicationListener) {
            this.applicationListener = applicationListener;
            return this;
        }

        public Builder<P> applicationDescriptor(
                final @Nullable ApplicationDescriptor applicationDescriptor) {
            this.applicationDescriptor = applicationDescriptor;
            return this;
        }

        public Builder<P> updateLocation(
                final @Nullable String updateLocation) {
            this.updateLocation = updateLocation;
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

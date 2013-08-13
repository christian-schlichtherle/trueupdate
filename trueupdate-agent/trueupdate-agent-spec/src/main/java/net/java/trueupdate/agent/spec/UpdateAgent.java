/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.agent.spec;

import java.net.URI;
import static java.util.Objects.requireNonNull;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import net.java.trueupdate.message.UpdateMessage;
import net.java.trueupdate.message.UpdateMessageException;

/**
 * An update agent cooperates with an update manager to automatically install
 * updates to an application.
 * All communication between the update agent and the update manager is
 * asynchronous.
 * <p>
 * Implementations should be immutable and hence, thread-safe.
 * <p>
 * Applications have no need to implement this class and should not do so
 * because it may be subject to future expansion.
 *
 * @author Christian Schlichtherle
 */
public interface UpdateAgent {

    /**
     * Sends a request to subscribe to the list of recipients for update
     * announcements for the application.
     *
     * @throws UpdateMessageException if sending the request is not possible
     *         for some reason.
     */
    void subscribe() throws UpdateMessageException;

    /**
     * Sends a request to shutdown the application, install the given version
     * and restart it.
     *
     * @param version the version to install.
     *        Specifying the current version shows no effect.
     *        Specifying a higher version upgrades the application.
     *        Specifying a prior version downgrades the application.
     * @throws UpdateMessageException if sending the request is not possible
     *         for some reason.
     */
    void install(String version) throws UpdateMessageException;

    /**
     * Sends a request to unsubscribe from the list of recipients for update
     * announcements for the application.
     *
     * @throws UpdateMessageException if sending the request is not possible
     *         for some reason.
     */
    void unsubscribe() throws UpdateMessageException;

    /**
     * A builder for update agents.
     * <p>
     * Implementations should be immutable and hence, thread-safe.
     * <p>
     * Applications have no need to implement this class and should not do so
     * because it may be subject to future expansion.
     */
    interface Builder {

        Parameters.Builder<Builder> parameters();

        Builder parameters(Parameters parameters);

        UpdateAgent build();
    }

    /** Update Agent Parameters. */
    final class Parameters {

        private final ApplicationDescriptor applicationDescriptor;
        private final URI updateLocation;
        private final UpdateListener updateListener;

        Parameters(final Builder<?> b) {
            this.applicationDescriptor = requireNonNull(b.applicationDescriptor);
            this.updateLocation = requireNonNull(b.updateLocation);
            this.updateListener = requireNonNull(b.updateListener);
        }

        /** Returns a new builder for update agent parameters. */
        public static Builder<Void> builder() { return new Builder<>(); }

        public ApplicationDescriptor applicationDescriptor() {
            return applicationDescriptor;
        }

        public URI updateLocation() { return updateLocation; }

        public UpdateListener updateListener() { return updateListener; }

        @SuppressWarnings(value = "PackageVisibleField")
        public static class Builder<T> {

            @CheckForNull ApplicationDescriptor applicationDescriptor;
            @CheckForNull URI updateLocation;
            @CheckForNull UpdateListener updateListener;

            protected Builder() { }

            public ApplicationDescriptor.Builder<Builder<T>> applicationDescriptor() {
                return new ApplicationDescriptor.Builder<Builder<T>>() {
                    @Override public Builder<T> inject() {
                        return updateAgentDescriptor(build());
                    }
                };
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

            public Builder<T> updateListener(
                    final @Nullable UpdateListener updateListener) {
                this.updateListener = updateListener;
                return this;
            }

            public Parameters build() { return new Parameters(this); }

            public T inject() {
                throw new IllegalStateException("No target for injection.");
            }
        } // Builder
    } // Parameters

    /** Processes update messages from an update manager. */
    abstract class UpdateListener {

        public void onSubscriptionSuccessResponse(UpdateMessage message)
        throws UpdateMessageException { }

        public void onSubscriptionFailureResponse(UpdateMessage message)
        throws UpdateMessageException { }

        public void onUpdateAnnouncement(UpdateMessage message)
        throws UpdateMessageException { }

        public void onInstallationSuccessResponse(UpdateMessage message)
        throws UpdateMessageException { }

        public void onInstallationFailureResponse(UpdateMessage message)
        throws UpdateMessageException { }

        public void onUnsubscriptionSuccessResponse(UpdateMessage message)
        throws UpdateMessageException { }

        public void onUnsubscriptionFailureResponse(UpdateMessage message)
        throws UpdateMessageException { }
    } // Listener
}

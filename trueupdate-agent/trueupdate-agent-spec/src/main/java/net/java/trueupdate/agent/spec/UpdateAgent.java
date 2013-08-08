/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.agent.spec;

import static java.util.Objects.requireNonNull;
import net.java.trueupdate.artifact.spec.ArtifactDescriptor;

/**
 * An agent for updating an application.
 * This component sends and receives message to and from the TrueUpdate Manager.
 * <p>
 * Implementations should be immutable and hence, thread-safe.
 * <p>
 * Applications have no need to implement this class and should not do so
 * because it may be subject to future expansion.
 *
 * @author Christian Schlichtherle
 */
public abstract class UpdateAgent {

    /**
     * Sends a request to subscribe to the list of recipients for update
     * announcements for the application.
     *
     * @param listener the call back interface for processing responses from
     *                 the TrueUpdate Manager.
     * @throws UpdateRuntimeException if sending the request is not possible for some
     *         reason.
     */
    public abstract void subscribe() throws UpdateRuntimeException;

    /**
     * Sends a request to unsubscribe from the list of recipients for update
     * announcements for the application.
     *
     * @throws UpdateRuntimeException if sending the request is not possible for some
     *         reason.
     */
    public abstract void unsubscribe() throws UpdateRuntimeException;

    /**
     * Sends a request to shutdown the application, install the given version
     * and restart it.
     *
     * @param version the version to install.
     *        Specifying the current version shows no effect.
     *        Specifying a higher version upgrades the application.
     *        Specifying a prior version downgrades the application.
     * @throws UpdateRuntimeException if sending the request is not possible for some
     *         reason.
     */
    public abstract void install(String version) throws UpdateRuntimeException;

    /** A builder for an update agent. */
    public static abstract class Builder {

        private ArtifactDescriptor artifactDescriptor;
        private UpdateListener updateListener;

        /** Returns the artifact descriptor for the application. */
        public final ArtifactDescriptor artifactDescriptor() {
            return artifactDescriptor;
        }

        /** Sets the artifact descriptor for the application. */
        public final Builder artifactDescriptor(
                final ArtifactDescriptor artifactDescriptor) {
            this.artifactDescriptor = requireNonNull(artifactDescriptor);
            return this;
        }

        /** Returns the update listener. */
        public final UpdateListener updateListener() { return updateListener; }

        /** Sets the update listener. */
        public final Builder updateListener(
                final UpdateListener updateListener) {
            this.updateListener = requireNonNull(updateListener);
            return this;
        }

        public abstract UpdateAgent build();
    }
}

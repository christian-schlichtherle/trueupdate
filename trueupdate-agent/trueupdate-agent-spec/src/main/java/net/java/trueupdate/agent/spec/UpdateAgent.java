/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.agent.spec;

/**
 * Communicates with the TrueUpdate Manager about updating this web app.
 * Note that all communication is asynchronous.
 * Implementations should be immutable and hence, thread-safe.
 *
 * @author Christian Schlichtherle
 */
public interface UpdateAgent {

    /** Returns the artifact descriptor for this web app. */
    ArtifactDescriptor artifactDescriptor();

    /**
     * Sends a message to the TrueUpdate Manager in order to subscribe to the
     * list of recipients for update announcements for this web app.
     *
     * @param listener the call back interface for processing responses from
     *                 the TrueUpdate Manager.
     * @throws Exception if sending the message is not possible for some reason.
     */
    void subscribe(UpdateListener listener) throws Exception;

    /**
     * Sends a message to the TrueUpdate Manager in order to unsubscribe from
     * the list of recipients for update announcements for this web app.
     *
     * @param listener the call back interface for processing responses from
     *                 the TrueUpdate Manager.
     * @throws Exception if sending the message is not possible for some reason.
     */
    void unsubscribe() throws Exception;

    void updateMeTo(String version) throws Exception;

    interface UpdateListener {

        void onSubscribeError(FailureEvent failure) throws Exception;

        void onUnsubscribeError(FailureEvent failure) throws Exception;

        void onUpdateAvailable(AnnouncementEvent announcement) throws Exception;
    }

    interface UpdateEvent {
        /** Returns the source update agent. */
        UpdateAgent source();
    }

    interface FailureEvent extends UpdateEvent {
        /**
         * Returns the reason for failure.
         * This may be a stack trace from the TrueUpdate Manager.
         */
        String reason();
    }

    interface AnnouncementEvent extends UpdateEvent {
        /** Returns the update version. */
        String updateVersion();
    }
}

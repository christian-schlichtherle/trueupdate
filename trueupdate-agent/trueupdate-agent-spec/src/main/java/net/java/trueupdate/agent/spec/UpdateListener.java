/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.agent.spec;

import net.java.trueupdate.message.UpdateMessage;

/**
 * Processes update events from the TrueUpdate Manager.
 * The implementation in this class ignores all messages except
 * {@link #onUpdateAvailable}.
 *
 * @author Christian Schlichtherle
 */
public abstract class UpdateListener {

    /** Processes the {@link UpdateMessageType#SUBSCRIBE_SUCCESS} message. */
    public void onSubscribeSuccess(UpdateEvent event) { }

    /** Processes the {@link UpdateMessageType#SUBSCRIBE_FAILURE} message. */
    public void onSubscribeFailure(UpdateEvent event) { }

    /** Processes the {@link UpdateMessageType#UNSUBSCRIBE_SUCCESS} message. */
    public void onUnsubscribeSuccess(UpdateEvent event) { }

    /** Processes the {@link UpdateMessageType#UNSUBSCRIBE_FAILURE} message. */
    public void onUnsubscribeFailure(UpdateEvent event) { }

    /**
     * Processes the {@link UpdateMessageType#UPDATE_AVAILABLE} message.
     * <p>
     * The implementation in the class {@link UpdateListener} sends a request
     * to install the available version.
     *
     * @param event the update event.
     *              The {@linkplain UpdateMessage#text() text} of the
     *              {@linkplain UpdateEvent#message() message} is the available
     *              version.
     */
    public void onUpdateAvailable(UpdateEvent event) {
        event.agent().install(event.message().text());
    }

    /** Processes the {@link UpdateMessageType#INSTALL_SUCCESS} message. */
    public void onInstallSuccess(UpdateEvent event) { }

    /** Processes the {@link UpdateMessageType#INSTALL_FAILURE} message. */
    public void onInstallFailure(UpdateEvent event) { }
}

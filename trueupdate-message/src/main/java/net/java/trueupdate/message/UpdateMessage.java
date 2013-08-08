/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.message;

import java.io.Serializable;

/**
 * A basic update message.
 *
 * @author Christian Schlichtherle
 */
public abstract class UpdateMessage {

    public static UpdateMessage success(Type type) {
        return new DefaultUpdateMessage(type, "SUCCESS");
    }

    public static UpdateMessage failure(Type type, Throwable ex) {
        return new DefaultUpdateMessage(type, ex.getMessage());
    }

    /** Returns the message type. */
    public abstract Type type();

    /** Returns the message text. */
    public abstract String text();

    @Override public String toString() {
        return String.format("%s: %s", type(), text());
    }

    /** The message type. */
    public enum Type implements Serializable {
        SUBSCRIBE_REQUEST, SUBSCRIBE_SUCCESS, SUBSCRIBE_FAILURE,
        UNSUBSCRIBE_REQUEST, UNSUBSCRIBE_SUCCESS, UNSUBSCRIBE_FAILURE,
        UPDATE_AVAILABLE,
        INSTALL_REQUEST, INSTALL_SUCCESS, INSTALL_FAILURE,
    }
}

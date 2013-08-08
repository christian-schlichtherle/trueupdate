/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.message;

import java.io.Serializable;

/**
 * A basic update message.
 *
 * @param <T> The type of the message body.
 * @author Christian Schlichtherle
 */
public abstract class UpdateMessage<B> {

    public static <B extends Serializable> UpdateMessage<B>
    success(Type<B> type, B body) {
        return new DefaultUpdateMessage<>(type, body);
    }

    public static UpdateMessage<String>
    failure(Type<String> type, Exception body) {
        return new DefaultUpdateMessage<>(type, body.toString());
    }

    /** Returns the message type. */
    public abstract Type<B> type();

    /** Returns the message body. */
    public abstract B body();

    @Override public String toString() {
        return String.format("%s: %s", type(), body());
    }

    /** The message type. */
    public interface Type<B> extends Serializable { }

    /** Void message types. */
    public enum VoidType implements Type<Void> {
        SUBSCRIBE_SUCCESS, UNSUBSCRIBE_SUCCESS, INSTALL_SUCCESS,
    }

    /** String message types. */
    public enum StringType implements Type<String> {
        SUBSCRIBE_FAILURE, UNSUBSCRIBE_FAILURE, INSTALL_FAILURE,
        UPDATE_AVAILABLE,
    }
}

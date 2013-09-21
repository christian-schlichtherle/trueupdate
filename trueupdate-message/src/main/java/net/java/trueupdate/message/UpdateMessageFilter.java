/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.message;

/**
 * @author Christian Schlichtherle
 */
public interface UpdateMessageFilter {

    UpdateMessageFilter ACCEPT_ALL = new UpdateMessageFilter() {
        @Override public boolean accept(UpdateMessage message) {
            return true;
        }
    };

    /**
     * Returns {@code true} if and only if this filter accepts the given
     * message.
     *
     * @param message the update message to filter.
     */
    boolean accept(UpdateMessage message);
}

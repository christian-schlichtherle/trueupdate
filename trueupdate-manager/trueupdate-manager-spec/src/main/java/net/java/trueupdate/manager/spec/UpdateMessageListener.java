/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.spec;

/**
 * Processes update messages.
 * <p>
 * Implementations should be immutable and hence, thread-safe.
 * <p>
 * Applications have no need to implement this class and should not do so
 * because it may be subject to future expansion.
 *
 * @see UpdateMessageDispatcher
 * @author Christian Schlichtherle
 */
public interface UpdateMessageListener {

    /**
     * Processes the given update message.
     *
     * @param message the update message to process.
     */
    void onUpdateMessage(UpdateMessage message) throws UpdateMessageException;
}

/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.message;

/**
 * An update listener processes update messages.
 *
 * @author Christian Schlichtherle
 */
public interface UpdateMessageListener {

    /** Processes the given update message. */
    void onUpdateMessage(UpdateMessage message);
}

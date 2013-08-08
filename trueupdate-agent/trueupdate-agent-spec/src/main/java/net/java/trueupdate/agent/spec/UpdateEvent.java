/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.agent.spec;

import net.java.trueupdate.message.UpdateMessage;

/**
 * An update event.
 *
 * @param <T> The type of the message body.
 * @author Christian Schlichtherle
 */
public interface UpdateEvent<B> {

    /** Returns the update message. */
    UpdateMessage<B> message();

    /** Returns the associated update agent. */
    UpdateAgent agent();
}

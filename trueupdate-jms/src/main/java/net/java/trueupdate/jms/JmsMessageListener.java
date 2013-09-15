/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.jms;

import java.io.Serializable;
import java.util.logging.*;
import javax.annotation.concurrent.Immutable;
import javax.jms.*;
import net.java.trueupdate.manager.spec.*;
import net.java.trueupdate.util.Objects;

/**
 * Listens to JMS messages, filters those with an embedded {@link UpdateMessage}
 * and forwards them to the given {@link UpdateMessageListener}.
 * All other JMS messages get silently ignored.
 *
 * @author Christian Schlichtherle
 */
@Immutable
public final class JmsMessageListener implements MessageListener {

    private static final Logger logger =
            Logger.getLogger(JmsMessageListener.class.getName());

    private final UpdateMessageListener updateMessageListener;

    public JmsMessageListener(
            final UpdateMessageListener updateMessageListener) {
        this.updateMessageListener =
                Objects.requireNonNull(updateMessageListener);
    }

    @Override public void onMessage(final Message message) {
        logger.log(Level.FINEST, "Received JMS message for update manager: {0}", message);
        try {
            if (message instanceof ObjectMessage) {
                final Serializable body = ((ObjectMessage) message).getObject();
                if (body instanceof UpdateMessage)
                    updateMessageListener.onUpdateMessage((UpdateMessage) body);
            }
        } catch (RuntimeException ex) {
            throw ex;
        } catch (final Exception ex) {
            logger.log(Level.SEVERE, "Could not process JMS message:", ex);
        }
    }
}

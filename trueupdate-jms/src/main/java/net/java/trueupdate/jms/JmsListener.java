/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.jms;

import java.util.logging.*;
import javax.annotation.concurrent.Immutable;
import javax.jms.*;
import net.java.trueupdate.message.*;
import net.java.trueupdate.util.Objects;

/**
 * Adapts the {@link net.java.trueupdate.message.UpdateMessageListener} class
 * to the {@link javax.jms.MessageListener} interface.
 * Instances of this class listen to JMS messages, filter those with an
 * embedded {@link net.java.trueupdate.message.UpdateMessage} and forward them
 * to the configured update message listener.
 * All other JMS messages get ignored.
 *
 * @author Christian Schlichtherle
 */
@Immutable
final class JmsListener implements MessageListener {

    private static final Logger logger =
            Logger.getLogger(JmsListener.class.getName());

    private final UpdateMessageListener updateMessageListener;

    JmsListener(final UpdateMessageListener uml) {
        this.updateMessageListener = Objects.requireNonNull(uml);
    }

    @Override public final void onMessage(final Message message) {
        logger.log(Level.FINEST, "Received JMS message {0} .", message);
        try {
            final String contentType = message.getStringProperty("contentType");
            if (null != contentType && contentType.startsWith("application/xml;")) {
                final String body = ((TextMessage) message).getText();
                updateMessageListener.onUpdateMessage(JAXB.decode(body));
            } else {
                logger.log(Level.WARNING, "Unsupported contentType property in JMS message {0} .", contentType);
            }
        } catch (final Exception ex) {
            logger.log(Level.WARNING, "Could not process JMS message:", ex);
        }
    }
}

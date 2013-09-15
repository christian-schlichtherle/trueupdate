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
 * Adapts the {@link UpdateMessageListener} class to the
 * {@link MessageListener} interface.
 * Instances of this class listen to JMS messages, filter those with an
 * embedded {@link UpdateMessage} and forward them to the update message
 * listener instance provided by the abstract method
 * {@link #updateMessageListener()}.
 * All other JMS messages get silently ignored.
 *
 * @author Christian Schlichtherle
 */
@Immutable
public abstract class JmsMessageListener implements MessageListener {

    private static final Logger logger =
            Logger.getLogger(JmsMessageListener.class.getName());

    public static JmsMessageListener create(
            final UpdateMessageListener updateMessageListener) {
        Objects.requireNonNull(updateMessageListener);
        return new JmsMessageListener() {
            @Override protected UpdateMessageListener updateMessageListener() {
                return updateMessageListener;
            }
        };
    }

    protected abstract UpdateMessageListener updateMessageListener();

    @Override public final void onMessage(final Message message) {
        logger.log(Level.FINEST, "Received JMS message for update manager: {0}", message);
        try {
            if (message instanceof ObjectMessage) {
                final Serializable body = ((ObjectMessage) message).getObject();
                if (body instanceof UpdateMessage)
                    updateMessageListener().onUpdateMessage((UpdateMessage) body);
            }
        } catch (RuntimeException ex) {
            throw ex;
        } catch (final Exception ex) {
            onException(ex);
            logger.log(Level.SEVERE, "Could not process JMS message:", ex);
        }
    }

    protected void onException(Exception ex) { }
}

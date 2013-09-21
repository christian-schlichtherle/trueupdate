/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.jms;

import java.io.Serializable;
import java.util.logging.*;
import javax.annotation.concurrent.Immutable;
import javax.jms.*;
import net.java.trueupdate.message.*;
import net.java.trueupdate.util.Objects;

/**
 * Adapts the {@link net.java.trueupdate.message.UpdateMessageListener} class to the
 * {@link MessageListener} interface.
 * Instances of this class listen to JMS messages, filter those with an
 * embedded {@link net.java.trueupdate.message.UpdateMessage} and forward them to the update message
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

    /** Returns a new JmsMessageListener with the given properties. */
    public static JmsMessageListener create(
            final UpdateMessageListener updateMessageListener) {
        Objects.requireNonNull(updateMessageListener);
        return new JmsMessageListener() {
            @Override protected UpdateMessageListener updateMessageListener() {
                return updateMessageListener;
            }
        };
    }

    @Override public final void onMessage(final Message message) {
        logger.log(Level.FINEST, "Received JMS message: {0}", message);
        try {
            if (message instanceof ObjectMessage) {
                final Serializable body = ((ObjectMessage) message).getObject();
                if (body instanceof UpdateMessage)
                    updateMessageListener().onUpdateMessage((UpdateMessage) body);
            }
        } catch (RuntimeException ex) {
            logger.log(Level.WARNING, "Could not process JMS message:", ex);
            throw ex;
        } catch (final Exception ex) {
            logger.log(Level.WARNING, "Could not process JMS message:", ex);
            onException(ex);
        }
    }

    /** Returns the update message listener. */
    protected abstract UpdateMessageListener updateMessageListener();

    /** Called on any non-{@link RuntimeException}. */
    protected void onException(Exception ex) { }
}

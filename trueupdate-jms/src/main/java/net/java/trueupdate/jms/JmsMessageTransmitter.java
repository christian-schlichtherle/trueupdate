/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.jms;

import java.util.logging.*;
import javax.annotation.WillNotClose;
import javax.annotation.concurrent.Immutable;
import javax.jms.*;
import javax.naming.*;
import net.java.trueupdate.manager.spec.UpdateMessage;
import static net.java.trueupdate.util.Objects.requireNonNull;

/**
 * Transmits {@link UpdateMessage}s via JMS.
 *
 * @author Christian Schlichtherle
 */
@Immutable
public abstract class JmsMessageTransmitter {

    private static final Logger logger =
            Logger.getLogger(JmsMessageTransmitter.class.getName());

    /**
     * Returns a new {@code JmsMessageTransmitter} with the given properties.
     *
     * @param context the context for looking up the destination using the
     *        {@link UpdateMessage#to()} property of the messages.
     * @param connectionFactory the factory for the connection for sending the
     *        messages.
     */
    public static JmsMessageTransmitter create(
            final Context context,
            final @WillNotClose ConnectionFactory connectionFactory) {
        requireNonNull(context);
        requireNonNull(connectionFactory);
        return new JmsMessageTransmitter() {
            @Override
            public void send(UpdateMessage message)
            throws NamingException, JMSException {
                send(message, destination(message, context), connectionFactory);
            }
        };
    }

    /**
     * Returns a new {@code JmsMessageTransmitter} with the given properties.
     *
     * @param context the context for looking up the destination using the
     *        {@link UpdateMessage#to()} property of the messages.
     * @param connectionFactory the connection for sending the messages.
     */
    public static JmsMessageTransmitter create(
            final Context context,
            final @WillNotClose Connection connection) {
        requireNonNull(context);
        requireNonNull(connection);
        return new JmsMessageTransmitter() {
            @Override
            public void send(UpdateMessage message)
            throws NamingException, JMSException {
                send(message, destination(message, context), connection);
            }
        };
    }

    static Destination destination(UpdateMessage message, Context context)
    throws NamingException {
        return (Destination) context.lookup(message.to());
    }

    static void send(
            final UpdateMessage message,
            final Destination destination,
            final ConnectionFactory connectionFactory)
    throws JMSException {
        final Connection connection = connectionFactory.createConnection();
        try {
            send(message, destination, connection);
        } finally {
            connection.close();
        }
    }

    static void send(
            final UpdateMessage message,
            final Destination destination,
            final @WillNotClose Connection connection)
    throws JMSException {
        final Session s = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        try {
            final Message m = s.createObjectMessage(message);
            m.setBooleanProperty("manager", message.type().forManager());
            s.createProducer(destination).send(m);
            logger.log(Level.FINEST, "Transmitted JMS message {0} to {1} .",
                    new Object[] { m, destination });
        } catch (final JMSException ex) {
            logger.log(Level.WARNING, "Could not transmit JMS message:", ex);
            throw ex;
        } finally {
            s.close();
        }
    }

    /** Transmits the given update message. */
    public abstract void send(UpdateMessage message)
    throws NamingException, JMSException;
}

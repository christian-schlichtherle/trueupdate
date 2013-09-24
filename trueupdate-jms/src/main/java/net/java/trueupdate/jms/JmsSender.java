/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.jms;

import java.util.logging.*;
import javax.annotation.WillNotClose;
import javax.annotation.concurrent.Immutable;
import javax.jms.*;
import javax.naming.*;
import net.java.trueupdate.message.UpdateMessage;
import static net.java.trueupdate.util.Objects.requireNonNull;

/**
 * Transmits {@link UpdateMessage}s via JMS.
 *
 * @author Christian Schlichtherle
 */
@Immutable
public abstract class JmsSender {

    private static final Logger logger =
            Logger.getLogger(JmsSender.class.getName());

    /**
     * Returns a new {@code JmsSender} with the given properties.
     *
     * @param namingContext the context for looking up the destination using
     *        the {@link UpdateMessage#to()} property of the messages.
     * @param connectionFactory the factory for the connection for sending the
     *        messages.
     */
    public static JmsSender create(
            final Context namingContext,
            final @WillNotClose ConnectionFactory connectionFactory) {
        requireNonNull(namingContext);
        requireNonNull(connectionFactory);
        return new JmsSender() {
            @Override
            public void send(UpdateMessage message) throws Exception {
                send(message, destination(message, namingContext), connectionFactory);
            }
        };
    }

    /**
     * Returns a new {@code JmsSender} with the given properties.
     *
     * @param namingContext the context for looking up the destination using
     *        the {@link UpdateMessage#to()} property of the messages.
     * @param connection the connection for sending the messages.
     */
    public static JmsSender create(
            final Context namingContext,
            final @WillNotClose Connection connection) {
        requireNonNull(namingContext);
        requireNonNull(connection);
        return new JmsSender() {
            @Override
            public void send(UpdateMessage message) throws Exception {
                send(message, destination(message, namingContext), connection);
            }
        };
    }

    static Destination destination(UpdateMessage message, Context namingContext)
    throws NamingException {
        return (Destination) namingContext.lookup(message.to());
    }

    static void send(
            final UpdateMessage message,
            final Destination destination,
            final ConnectionFactory connectionFactory)
    throws Exception {
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
    throws Exception {
        final Session s = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        try {
            final Message m = s.createTextMessage(JAXB.encode(message));
            m.setStringProperty("contentType", "application/xml; charset=utf-8");
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
    public abstract void send(UpdateMessage message) throws Exception;
}

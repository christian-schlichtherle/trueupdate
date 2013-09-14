/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.mini;

import java.io.Serializable;
import java.util.logging.*;
import javax.annotation.WillCloseWhenClosed;
import javax.annotation.concurrent.ThreadSafe;
import javax.jms.*;
import net.java.trueupdate.manager.core.UpdateManager;
import net.java.trueupdate.manager.spec.UpdateMessage;

/**
 * @author Christian Schlichtherle
 */
@ThreadSafe
final class MiniListener implements Runnable {

    private static final Logger logger =
            Logger.getLogger(MiniListener.class.getName());

    private static final String SUBSCRIPTION_NAME = "TrueUpdate Manager";
    private static final String MESSAGE_SELECTOR = "manager = true";
    private static final boolean NO_LOCAL = true;

    private final UpdateManager updateManager;
    private final @WillCloseWhenClosed Connection connection;
    private final Destination destination;

    private MessageConsumer messageConsumer;

    MiniListener(
            final UpdateManager updateManager,
            final ConnectionFactory factory,
            final Destination origin) throws JMSException {
        assert null != updateManager;
        assert null != origin;
        this.updateManager = updateManager;
        this.connection = factory.createConnection();
        this.destination = origin;
    }

    @Override public void run() {
        try {
            final Connection c = connection;
            try {
                final Session s = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
                final Destination d = destination;
                messageConsumer = d instanceof Topic
                        ? s.createDurableSubscriber((Topic) d, SUBSCRIPTION_NAME, MESSAGE_SELECTOR, NO_LOCAL)
                        : s.createConsumer(d, MESSAGE_SELECTOR);
                c.start();
                for (Message m; null != (m = messageConsumer.receive()); ) {
                    synchronized(this) {
                        if (null == messageConsumer) break;
                        m.acknowledge();
                        onMessage(m);
                    }
                }
            } finally {
                c.close();
            }
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Could not set up JMS.", ex);
        }
    }

    private void onMessage(final Message message) {
        logger.log(Level.FINEST, "Received JMS message for update manager: {0}", message);
        try {
            if (message instanceof ObjectMessage) {
                final Serializable body = ((ObjectMessage) message).getObject();
                if (body instanceof UpdateMessage)
                    updateManager.onUpdateMessage((UpdateMessage) body);
            }
        } catch (RuntimeException ex) {
            throw ex;
        } catch (final Exception ex) {
            logger.log(Level.SEVERE, "Could not process JMS message.", ex);
        }
    }

    /**
     * Asynchronously closes this message listener.
     * After the call to this method, the client may safely assume that this
     * message listener will not call {@link UpdateManager#onUpdateMessage}
     * anymore.
     */
    public synchronized void close() throws JMSException {
        // HC SUNT DRACONIS!
        final MessageConsumer mc = messageConsumer;
        if (null != mc) {
            mc.close();
            messageConsumer = null;
        }
    }
}

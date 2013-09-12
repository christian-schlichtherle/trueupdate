/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.mini;

import java.io.Serializable;
import java.util.logging.*;
import javax.jms.*;
import net.java.trueupdate.installer.core.UpdateManager;
import net.java.trueupdate.manager.spec.UpdateMessage;

/**
 * @author Christian Schlichtherle
 */
final class UpdateManagerMessageListener implements Runnable, MessageListener {

    private static final Logger logger =
            Logger.getLogger(UpdateManagerMessageListener.class.getName());

    private static final String SUBSCRIPTION_NAME = "TrueUpdate Manager";
    private static final String MESSAGE_SELECTOR = "manager = true";
    private static final boolean NO_LOCAL = true;

    private final Connection connection;
    private final Destination destination;
    private final UpdateManager updateManager;

    private volatile MessageConsumer messageConsumer;

    UpdateManagerMessageListener(
            final UpdateManager um,
            final Connection c,
            final Destination d) {
        assert null != um;
        assert null != c;
        assert null != d;
        this.updateManager = um;
        this.connection = c;
        this.destination = d;
    }

    public void close() throws Exception {
        final MessageConsumer mc = messageConsumer;
        if (null != mc) mc.close();
    }

    @Override public void run() {
        try {
            final Connection c = connection;
            try {
                final Session s = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
                final Destination d = destination;
                messageConsumer = d instanceof Topic
                        ? s.createDurableSubscriber((Topic) d, SUBSCRIPTION_NAME, MESSAGE_SELECTOR, NO_LOCAL)
                        : s.createConsumer(d, MESSAGE_SELECTOR);
                c.start();
                for (Message m; null != (m = messageConsumer.receive()); )
                    onMessage(m);
            } finally {
                c.close();
            }
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Could not set up JMS.", ex);
        }
    }

    @Override public void onMessage(final Message message) {
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
}

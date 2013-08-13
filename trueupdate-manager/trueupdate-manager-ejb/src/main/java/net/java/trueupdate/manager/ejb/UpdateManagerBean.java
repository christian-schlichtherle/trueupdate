/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.ejb;

import java.util.logging.*;
import javax.annotation.*;
import javax.ejb.*;
import javax.jms.*;
import javax.naming.*;
import net.java.trueupdate.message.*;

/**
 * @author Christian Schlichtherle
 */
@Singleton
@Local(UpdateMessageListener.class)
public class UpdateManagerBean extends UpdateMessageListener {

    private static final Logger
            logger = Logger.getLogger(UpdateManagerBean.class.getName());

    @Resource
    private ConnectionFactory connectionFactory;

    private Connection connection;

    @PostConstruct private void postConstruct() {
        try {
            connection = connectionFactory.createConnection();
        } catch (JMSException ex) {
            logger.log(Level.SEVERE, "Error while creating connection.", ex);
        }
    }

    @PreDestroy private void preDestroy() {
        try {
            connection.close();
        } catch (JMSException ex) {
            logger.log(Level.SEVERE, "Error while closing connection.", ex);
        }
    }

    @Override protected void onSubscriptionRequest(final UpdateMessage message)
    throws UpdateMessageException {
        logger.log(Level.INFO, "Processing subscription request:\n{0}", message.toString());
        respondTo(message);
    }

    @Override protected void onInstallationRequest(final UpdateMessage message)
    throws UpdateMessageException {
        logger.log(Level.INFO, "Processing installation request:\n{0}", message.toString());
        respondTo(message);
    }

    @Override protected void onUnsubscriptionRequest(final UpdateMessage message)
    throws UpdateMessageException {
        logger.log(Level.INFO, "Processing unsubscribtion request:\n{0}", message.toString());
        respondTo(message);
    }

    private void respondTo(UpdateMessage request) throws UpdateMessageException {
        send(request.successResponse());
    }

    private void send(final UpdateMessage message) throws UpdateMessageException {
        try {
            final Destination destination = destination(message);
            final Session session = connection.createSession(true, 0);
            try {
                session .createProducer(destination)
                        .send(session.createObjectMessage(message));
            } finally {
                session.close();
            }
        } catch (Exception ex) {
            throw new UpdateMessageException(ex);
        }
    }

    private Destination destination(UpdateMessage message)
    throws NamingException {
        return InitialContext.doLookup(message.to().toString());
    }
}

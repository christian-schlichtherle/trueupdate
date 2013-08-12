/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.ejb;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;
import net.java.trueupdate.message.*;

/**
 * @author Christian Schlichtherle
 */
@Singleton
public class UpdateManagerBean extends UpdateMessageListener {

    private static final Logger
            logger = Logger.getLogger(UpdateManagerBean.class.getName());

    @Resource
    private ConnectionFactory connectionFactory;

    private Connection connection;

    @PostConstruct
    private void postConstruct() {
        try {
            connection = connectionFactory.createConnection();
        } catch (JMSException ex) {
            logger.log(Level.SEVERE, "Error while creating connection.", ex);
        }
    }

    @PreDestroy
    private void preDestroy() {
        try {
            connection.close();
        } catch (JMSException ex) {
            logger.log(Level.SEVERE, "Error while closing connection.", ex);
        }
    }

    @Override protected void onSubscriptionRequest(UpdateMessage message)
    throws UpdateMessageException {
        logger.log(Level.INFO, "Processing subscription request:\n{0}", message.toString());
    }

    @Override protected void onInstallationRequest(UpdateMessage message)
    throws UpdateMessageException {
        logger.log(Level.INFO, "Processing installation request:\n{0}", message.toString());
    }

    @Override protected void onUnsubscriptionRequest(UpdateMessage message)
    throws UpdateMessageException {
        logger.log(Level.INFO, "Processing unsubscribtion request:\n{0}", message.toString());
    }

    private void respondTo(UpdateMessage request) throws UpdateMessageException {
        send(request.successResponse());
    }

    private void send(final UpdateMessage message) throws UpdateMessageException {
        try {
            final Session session = connection.createSession(true, 0);
            try {
                session.createProducer(destination(message));
            } finally {
                session.close();
            }
        } catch (Exception ex) {
            throw new UpdateMessageException(ex);
        }
    }

    private static Destination destination(UpdateMessage message) {
        throw new UnsupportedOperationException();
    }
}

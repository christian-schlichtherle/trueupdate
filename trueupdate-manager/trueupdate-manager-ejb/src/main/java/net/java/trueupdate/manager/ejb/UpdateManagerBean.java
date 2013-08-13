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
import net.java.trueupdate.manager.spec.*;

/**
 * @author Christian Schlichtherle
 */
@Singleton
public class UpdateManagerBean
extends UpdateMessageDispatcher implements UpdateMessageListener {

    private static final Logger
            logger = Logger.getLogger(UpdateManagerBean.class.getName());

    @Resource
    private ConnectionFactory connectionFactory;

    private Connection connection;

    @PostConstruct private void postConstruct() {
        try {
            connection = connectionFactory.createConnection();
        } catch (RuntimeException ex) {
            throw ex;
        } catch (JMSException ex) {
            logger.log(Level.SEVERE, "Error while creating connection.", ex);
        }
    }

    @PreDestroy private void preDestroy() {
        try {
            connection.close();
        } catch (RuntimeException ex) {
            throw ex;
        } catch (JMSException ex) {
            logger.log(Level.SEVERE, "Error while closing connection.", ex);
        }
    }

    @Override protected void onSubscriptionRequest(final UpdateMessage message)
    throws Exception {
        logResponse(respondTo(logRequest(message)));
    }

    @Override protected void onInstallationRequest(final UpdateMessage message)
    throws Exception {
        logResponse(respondTo(logRequest(message)));
    }

    @Override protected void onUnsubscriptionRequest(final UpdateMessage message)
    throws Exception {
        logResponse(respondTo(logRequest(message)));
    }

    private UpdateMessage logRequest(final UpdateMessage request) {
        logger.log(Level.FINE, "Received update message:\n{0}", request);
        return request;
    }

    private UpdateMessage logResponse(final UpdateMessage response) {
        logger.log(Level.FINER, "Sent update message:\n{0}", response);
        return response;
    }

    private UpdateMessage respondTo(UpdateMessage request) throws Exception {
        return send(request.successResponse());
    }

    private UpdateMessage send(final UpdateMessage response) throws Exception {
        final Destination destination = destination(response);
        final Session session = connection.createSession(true, 0);
        try {
            final Message message = session.createObjectMessage(response);
            message.setBooleanProperty("request", false);
            session.createProducer(destination).send(message);
            return response;
        } finally {
            session.close();
        }
    }

    private Destination destination(UpdateMessage message)
    throws NamingException {
        return InitialContext.doLookup(message.to().toString());
    }
}

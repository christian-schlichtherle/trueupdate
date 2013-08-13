/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.ejb;

import net.java.trueupdate.manager.spec.UpdateMessageDispatcher;
import net.java.trueupdate.manager.spec.UpdateMessage;
import net.java.trueupdate.manager.spec.UpdateMessageException;
import java.util.logging.*;
import javax.annotation.*;
import javax.ejb.*;
import javax.jms.*;
import javax.naming.*;

/**
 * @author Christian Schlichtherle
 */
@Singleton
public class UpdateManagerBean
extends UpdateMessageDispatcher implements UpdateManager {

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
    throws UpdateMessageException {
        log(message);
        respondTo(message);
    }

    @Override protected void onInstallationRequest(final UpdateMessage message)
    throws UpdateMessageException {
        log(message);
        respondTo(message);
    }

    @Override protected void onUnsubscriptionRequest(final UpdateMessage message)
    throws UpdateMessageException {
        log(message);
        respondTo(message);
    }

    private UpdateMessage log(final UpdateMessage message) {
        logger.log(Level.INFO, "Received update message:\n{0}", message);
        return message;
    }

    private UpdateMessage respondTo(UpdateMessage request)
    throws UpdateMessageException {
        return send(request.successResponse());
    }

    private UpdateMessage send(final UpdateMessage message)
    throws UpdateMessageException {
        try {
            final Destination destination = destination(message);
            final Session session = connection.createSession(true, 0);
            try {
                session .createProducer(destination)
                        .send(session.createObjectMessage(message));
                return message;
            } finally {
                session.close();
            }
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new UpdateMessageException(ex);
        }
    }

    private Destination destination(UpdateMessage message)
    throws NamingException {
        return InitialContext.doLookup(message.to().toString());
    }
}

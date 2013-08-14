/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.impl.javaee;

import java.util.*;
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

    @EJB
    private UpdateInstaller installer;

    private final Map<ApplicationDescriptor, UpdateMessage>
            subscriptions = new HashMap<>();

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

    @Override protected void onSubscriptionRequest(UpdateMessage message)
    throws Exception {
        logResponse(respondWith(subscribe(logRequest(message))));
    }

    private UpdateMessage subscribe(final UpdateMessage message) {
        subscriptions.put(message.applicationDescriptor(), message);
        return message.successResponse();
    }

    @Override protected void onInstallationRequest(UpdateMessage message)
    throws Exception {
        logResponse(respondWith(install(logRequest(message))));
    }

    private UpdateMessage install(UpdateMessage message) {
        try {
            installer.install(message);
            return message.successResponse();
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            return message.failureResponse(ex);
        }
    }

    @Override protected void onUnsubscriptionRequest(UpdateMessage message)
    throws Exception {
        logResponse(respondWith(unsubscribe(logRequest(message))));
    }

    private UpdateMessage unsubscribe(final UpdateMessage message) {
        subscriptions.remove(message.applicationDescriptor());
        return message.successResponse();
    }

    private UpdateMessage logRequest(final UpdateMessage request) {
        logger.log(Level.FINE, "Received update message:\n{0}", request);
        return request;
    }

    private UpdateMessage logResponse(final UpdateMessage response) {
        logger.log(Level.FINER, "Sent update message:\n{0}", response);
        return response;
    }

    private UpdateMessage respondWith(final UpdateMessage response)
    throws Exception {
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

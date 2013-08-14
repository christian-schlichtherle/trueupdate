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
import static net.java.trueupdate.manager.spec.UpdateMessage.Type.*;

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
            persistSubscriptions();
            connection.close();
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error while closing connection.", ex);
        }
    }

    private void persistSubscriptions() throws Exception {
        for (UpdateMessage um : subscriptions.values())
            send(um.type(SUBSCRIPTION_NOTICE));
    }

    @Override protected void onSubscriptionNotice(UpdateMessage message)
    throws Exception {
        logResponse(subscribe(logRequest(message)));
    }

    @Override protected void onSubscriptionRequest(UpdateMessage message)
    throws Exception {
        logResponse(send(subscribe(logRequest(message))));
    }

    private UpdateMessage subscribe(final UpdateMessage message) {
        subscriptions.put(message.applicationDescriptor(), message);
        return message.successResponse();
    }

    @Override protected void onInstallationRequest(UpdateMessage message)
    throws Exception {
        logResponse(send(install(logRequest(message))));
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

    @Override protected void onUnsubscriptionNotice(UpdateMessage message)
    throws Exception {
        logResponse(unsubscribe(logRequest(message)));
    }

    @Override protected void onUnsubscriptionRequest(UpdateMessage message)
    throws Exception {
        logResponse(send(unsubscribe(logRequest(message))));
    }

    private UpdateMessage unsubscribe(final UpdateMessage message) {
        subscriptions.remove(message.applicationDescriptor());
        return message.successResponse();
    }

    private UpdateMessage logRequest(final UpdateMessage request) {
        logger.log(Level.INFO, "Update message request:\n{0}", request);
        return request;
    }

    private UpdateMessage logResponse(final UpdateMessage response) {
        logger.log(Level.FINER, "Update message response:\n{0}", response);
        return response;
    }

    private UpdateMessage send(final UpdateMessage um)
    throws Exception {
        final Destination d = to(um);
        final Session s = connection.createSession(true, 0);
        try {
            final Message m = s.createObjectMessage(um);
            m.setBooleanProperty("manager", um.type().manager());
            s.createProducer(d).send(m);
            return um;
        } finally {
            s.close();
        }
    }

    private Destination to(UpdateMessage message) throws NamingException {
        return InitialContext.doLookup(message.to().toString());
    }
}

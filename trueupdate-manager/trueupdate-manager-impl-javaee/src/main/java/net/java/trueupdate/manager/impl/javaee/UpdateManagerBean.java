/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.impl.javaee;

import java.net.*;
import java.util.*;
import java.util.logging.*;
import javax.annotation.*;
import javax.ejb.*;
import javax.jms.*;
import net.java.trueupdate.core.util.SystemProperties;
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

    @Resource(name = "serverBaseUri")
    private String serverBaseString;

    @Resource
    private ConnectionFactory connectionFactory;

    private Connection connection;

    @EJB
    private UpdateInstaller installer;

    @Resource(name = "TrueUpdate")
    Topic destination;

    private final Map<ApplicationDescriptor, UpdateMessage>
            subscriptions = new HashMap<>();

    private URI serverBaseUri() throws URISyntaxException {
        return new URI(SystemProperties.resolve(serverBaseString));
    }

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
        logOutput(subscribe(logInput(message)));
    }

    @Override protected void onSubscriptionRequest(UpdateMessage message)
    throws Exception {
        logOutput(send(subscribe(logInput(message))));
    }

    private UpdateMessage subscribe(final UpdateMessage message) {
        subscriptions.put(message.applicationDescriptor(), message);
        try {
            logger.info(serverBaseUri().toString());
        } catch (Exception ex) {
            Logger.getLogger(UpdateManagerBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        return message.successResponse();
    }

    @Override protected void onInstallationRequest(UpdateMessage message)
    throws Exception {
        logOutput(send(install(logInput(message))));
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
        logOutput(unsubscribe(logInput(message)));
    }

    @Override protected void onUnsubscriptionRequest(UpdateMessage message)
    throws Exception {
        logOutput(send(unsubscribe(logInput(message))));
    }

    private UpdateMessage unsubscribe(final UpdateMessage message) {
        subscriptions.remove(message.applicationDescriptor());
        return message.successResponse();
    }

    private UpdateMessage logInput(final UpdateMessage message) {
        logger.log(Level.FINE, "Input update message:\n{0}", message);
        return message;
    }

    private UpdateMessage logOutput(final UpdateMessage message) {
        logger.log(Level.FINER, "Output update message:\n{0}", message);
        return message;
    }

    private UpdateMessage send(final UpdateMessage message) throws Exception {
        final Session s = connection.createSession(true, 0);
        try {
            final Message m = s.createObjectMessage(message);
            m.setBooleanProperty("manager", message.type().forManager());
            s.createProducer(destination).send(m);
            return message;
        } finally {
            s.close();
        }
    }
}

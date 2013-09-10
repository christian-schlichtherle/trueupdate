/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.javaee;

import java.net.*;
import java.util.concurrent.Callable;
import java.util.logging.*;
import javax.annotation.*;
import javax.ejb.*;
import javax.inject.Inject;
import javax.jms.*;
import net.java.trueupdate.installer.core.*;
import net.java.trueupdate.jaxrs.client.UpdateClient;
import net.java.trueupdate.manager.spec.*;
import net.java.trueupdate.util.SystemProperties;

/**
 * An update manager bean.
 *
 * @author Christian Schlichtherle
 */
@Singleton
public class UpdateManagerBean extends UpdateManager {

    private static final Logger
            logger = Logger.getLogger(UpdateManagerBean.class.getName());

    @Resource(name = "connectionFactory")
    private ConnectionFactory connectionFactory;

    @Resource(name = "destination", lookup = "jms/TrueUpdate Agent")
    private Destination destination;

    private Connection connection;

    @Resource(name = "updateServiceBaseUri")
    private String updateServiceBaseString;

    @Resource(name = "checkUpdatesIntervalMinutes")
    private int checkUpdatesIntervalMinutes;

    @Resource(name = "timerService")
    private TimerService timerService;

    @Inject
    private UpdateInstaller installer;

    private UpdateClient client;

    @PostConstruct private void postConstruct() {
        wrap(new Callable<Void>() {
            @Override public Void call() throws Exception {
                open();
                return null;
            }
        });
    }

    @PreDestroy private void preDestroy() {
        wrap(new Callable<Void>() {
            @Override public Void call() throws Exception {
                close();
                return null;
            }
        });
    }

    private static @Nullable <V> V wrap(final Callable<V> task) {
        try {
            return task.call();
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new EJBException(ex);
        }
    }

    private void open() throws Exception {
        initClient();
        initConnection();
        initTimer();
    }

    private void initClient() throws URISyntaxException {
        client = new UpdateClient(new URI(SystemProperties.resolve(
                updateServiceBaseString)));
    }

    private void initConnection() throws JMSException {
        connection = connectionFactory.createConnection();
    }

    private void initTimer() {
        logger.log(Level.CONFIG,
                "The configured interval for update checks is {0} minutes.",
                checkUpdatesIntervalMinutes);
        final long intervalMillis = checkUpdatesIntervalMinutes * 60L * 1000;
        timerService.createTimer(intervalMillis, intervalMillis, null);
    }

    @Override public void close() throws Exception {
        try { super.close(); }
        finally { closeConnection(); }
    }

    private void closeConnection() throws JMSException { connection.close(); }

    @Override protected UpdateClient updateClient() { return client; }

    @Override protected UpdateInstaller updateInstaller() { return installer; }

    @Timeout @Override protected void checkUpdates() throws Exception {
        super.checkUpdates();
    }

    @Override
    protected UpdateMessage send(final UpdateMessage message) throws Exception {
        final Session s = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        try {
            final Message m = s.createObjectMessage(message);
            m.setBooleanProperty("manager", message.type().forManager());
            s.createProducer(destination).send(m);
        } finally {
            s.close();
        }
        return message;
    }
}

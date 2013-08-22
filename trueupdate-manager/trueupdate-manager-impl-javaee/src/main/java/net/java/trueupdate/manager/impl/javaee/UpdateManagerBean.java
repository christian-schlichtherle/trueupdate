/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.impl.javaee;

import java.lang.reflect.UndeclaredThrowableException;
import java.net.*;
import java.util.concurrent.Callable;
import java.util.logging.*;
import javax.annotation.*;
import javax.ejb.*;
import javax.inject.Inject;
import javax.jms.*;
import net.java.trueupdate.core.util.SystemProperties;
import net.java.trueupdate.jax.rs.client.UpdateClient;
import net.java.trueupdate.manager.core.*;
import net.java.trueupdate.manager.spec.UpdateMessage;

/**
 * An update manager bean.
 *
 * @author Christian Schlichtherle
 */
@Singleton
public class UpdateManagerBean extends BasicUpdateManager {

    private static final Logger
            logger = Logger.getLogger(UpdateManagerBean.class.getName());

    @Resource(name = "updateServiceBaseUri")
    private String updateServiceBaseString;

    @Resource
    private ConnectionFactory connectionFactory;

    private Connection connection;

    @Inject
    private UpdateInstaller installer;

    @Resource(name = "TrueUpdate")
    private Topic destination;

    @Resource(name = "checkUpdatesIntervalMinutes")
    private int checkUpdatesIntervalMinutes;

    @Resource
    private TimerService timerService;

    @Override protected UpdateClient updateClient() {
        return new UpdateClient(updateServiceBaseUri());
    }

    private URI updateServiceBaseUri() {
        return URI.create(SystemProperties.resolve(updateServiceBaseString));
    }

    @Override protected UpdateInstaller updateInstaller() { return installer; }

    @PostConstruct private void init() {
        wrap(new Callable<Void>() {
            @Override public Void call() throws Exception {
                initConnection();
                initTimer();
                return null;
            }
        });
    }

    private void initConnection() throws JMSException {
        connection = connectionFactory.createConnection();
    }

    private void initTimer() {
        logger.log(Level.CONFIG,
                "The configured update interval is {0} minutes.",
                checkUpdatesIntervalMinutes);
        final long intervalMillis = checkUpdatesIntervalMinutes * 60L * 1000;
        timerService.createTimer(intervalMillis, intervalMillis, null);
    }

    @PreDestroy private void preDestroy() {
        wrap(new Callable<Void>() {
            @Override public Void call() throws Exception {
                try {
                    shutdown();
                } finally {
                    closeConnection();
                }
                return null;
            }
        });
    }

    private void closeConnection() throws JMSException { connection.close(); }

    private static @Nullable <V> V wrap(final Callable<V> task) {
        try {
            return task.call();
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new UndeclaredThrowableException(ex);
        }
    }

    @Timeout @Override protected void checkUpdates() throws Exception {
        super.checkUpdates();
    }

    @Override
    protected UpdateMessage send(final UpdateMessage message) throws Exception {
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

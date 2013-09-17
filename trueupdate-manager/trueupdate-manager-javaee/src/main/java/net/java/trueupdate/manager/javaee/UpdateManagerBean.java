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
import javax.naming.*;
import net.java.trueupdate.jaxrs.client.UpdateClient;
import net.java.trueupdate.jms.JmsMessageSender;
import net.java.trueupdate.manager.core.UpdateManager;
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

    @Resource(name = "updateServiceBaseUri")
    private String updateServiceBaseString;

    @Resource(name = "checkUpdatesIntervalMinutes")
    private int checkUpdatesIntervalMinutes;

    @Resource(name = "timerService")
    private TimerService timerService;

    @Resource(name = "connectionFactory")
    private ConnectionFactory connectionFactory;

    @Inject
    private UpdateInstaller updateInstaller;

    private Connection connection;

    private Context namingContext;

    private UpdateClient updateClient;

    private Timer timer;

    @PostConstruct private void postConstruct() {
        if (null != timer) return;
        wrap(new Callable<Void>() {
            @Override public Void call() throws Exception {
                open();
                return null;
            }
        });
    }

    @PreDestroy private void preDestroy() {
        if (null == timer) return;
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
        // HC SUNT DRACONIS!
        namingContext = newNamingContext();
        updateClient = newUpdateClient();
        connection = newConnection();
        timer = newTimer();
    }

    private Context newNamingContext() throws NamingException {
        return (Context) new InitialContext().lookup("java:comp/env");
    }

    private UpdateClient newUpdateClient() throws URISyntaxException {
        return new UpdateClient(new URI(SystemProperties.resolve(
                updateServiceBaseString)));
    }

    private Timer newTimer() {
        logger.log(Level.CONFIG,
                "Interval for checking for updates is {0} minutes.",
                checkUpdatesIntervalMinutes);
        final long intervalMillis = checkUpdatesIntervalMinutes * 60L * 1000;
        return timerService.createTimer(intervalMillis, intervalMillis, null);
    }

    private Connection newConnection() throws JMSException {
        return connectionFactory.createConnection();
    }

    @Timeout @Override public void checkUpdates() throws Exception {
        super.checkUpdates();
    }

    @Override
    protected void send(UpdateMessage message) throws Exception {
        JmsMessageSender.create(namingContext, connection).send(message);
    }

    @Override public void close() throws Exception {
        // HC SUNT DRACONIS!
        timer.cancel();
        super.close();
        connection.close();
        timer = null;
        connection = null;
    }

    @Override protected UpdateClient updateClient() { return updateClient; }

    @Override protected UpdateInstaller updateInstaller() {
        return updateInstaller;
    }
}

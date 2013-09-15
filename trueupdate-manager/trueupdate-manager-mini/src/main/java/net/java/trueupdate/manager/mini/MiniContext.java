/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.mini;

import java.net.URI;
import java.util.ServiceLoader;
import java.util.logging.*;
import javax.annotation.concurrent.Immutable;
import javax.jms.*;
import javax.naming.*;
import javax.servlet.ServletContext;
import net.java.trueupdate.jaxrs.client.UpdateClient;
import net.java.trueupdate.jms.JmsMessageListener;
import net.java.trueupdate.jms.JmsMessageLoop;
import net.java.trueupdate.manager.spec.UpdateInstaller;

/**
 * Provides the objects required for this package.
 *
 * @author Christian Schlichtherle
 */
@Immutable
final class MiniContext {

    private static final Logger
            logger = Logger.getLogger(MiniContext.class.getName());

    private final ServletContext servletContext;
    private final Context namingContext;
    private final MiniUpdateManager manager;
    private final MiniTimer timer;
    private final JmsMessageLoop loop;

    MiniContext(final ServletContext servletContext)
    throws NamingException, JMSException {
        this.servletContext = servletContext;
        namingContext = new InitialContext();
        final ConnectionFactory connectionFactory = (ConnectionFactory)
                lookup("jms/ConnectionFactory");
        manager = new MiniUpdateManager(
                connectionFactory,
                namingContext,
                updateClient(),
                updateInstaller());
        timer = new MiniTimer(manager,
                checkUpdatesIntervalMinutes());
        loop = JmsMessageLoop
                .builder()
                .connectionFactory(connectionFactory)
                .destination((Destination) lookup("jms/TrueUpdate Manager"))
                .subscriptionName("TrueUpdate Manager")
                .messageSelector("manager = true")
                .messageListener(new JmsMessageListener(manager))
                .build();
    }

    void start() {
        timer().start();
        listener().start();
    }

    private Thread timer() {
        return new Thread(timer, "TrueUpdate Manager Mini Timer Daemon") {
            { super.setDaemon(true); }
        };
    }

    private Thread listener() {
        return new Thread(loop, "TrueUpdate Manager Mini Listener Daemon") {
            { super.setDaemon(true); }
        };
    }

    private UpdateClient updateClient() {
        return new UpdateClient(updateServiceBaseUri());
    }

    private URI updateServiceBaseUri() {
        final URI usburi = URI.create(initParameter("updateServiceBaseUri"));
        logger.log(Level.CONFIG, "Base URI of the update service is {0} .", usburi);
        return usburi;
    }

    private int checkUpdatesIntervalMinutes() {
        final int cuim = Integer.parseInt(initParameter("checkUpdatesIntervalMinutes"));
        logger.log(Level.CONFIG, "Interval for checking for updates is {0} minutes.", cuim);
        return cuim;
    }

    private UpdateInstaller updateInstaller() {
        final UpdateInstaller ui = ServiceLoader.load(
                UpdateInstaller.class,
                Thread.currentThread().getContextClassLoader()
                ).iterator().next();
        logger.log(Level.CONFIG, "UpdateInstaller class is {0} .", ui.getClass());
        return ui;
    }

    private Object lookup(String name) throws NamingException {
        return namingContext.lookup(initParameter(name));
    }

    private String initParameter(String name) {
        return servletContext.getInitParameter(name);
    }

    void stop() throws Exception {
        // HC SUNT DRACONIS!
        loop.stop();
        timer.stop();
        manager.close();
    }
}

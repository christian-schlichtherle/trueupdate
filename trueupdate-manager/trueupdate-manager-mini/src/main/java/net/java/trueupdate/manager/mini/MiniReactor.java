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
import net.java.trueupdate.manager.spec.UpdateInstaller;

/**
 * Breeds the objects required for this package.
 *
 * @author Christian Schlichtherle
 */
@Immutable
final class MiniReactor {

    private static final Logger
            logger = Logger.getLogger(MiniReactor.class.getName());

    private final ServletContext servletContext;
    private final InitialContext initialContext;
    private final MiniUpdateManager manager;
    private final MiniTimer timer;
    private final MiniListener listener;

    MiniReactor(final ServletContext servletContext)
    throws NamingException, JMSException {
        this.servletContext = servletContext;
        this.initialContext = new InitialContext();
        final ConnectionFactory connectionFactory = connectionFactory();
        this.manager = new MiniUpdateManager(
                connectionFactory,
                destination("jms/TrueUpdate Agent"),
                updateClient(),
                updateInstaller());
        this.timer = new MiniTimer(manager,
                checkUpdatesIntervalMinutes());
        this.listener = new MiniListener(manager,
                connectionFactory,
                destination("jms/TrueUpdate Manager"));
    }

    Thread timer() {
        return new Thread(timer, "TrueUpdate Manager Mini Timer Daemon") {
            { super.setDaemon(true); }
        };
    }

    Thread listener() {
        return new Thread(listener, "TrueUpdate Manager Mini Listener Daemon") {
            { super.setDaemon(true); }
        };
    }

    private ConnectionFactory connectionFactory() throws NamingException {
            return (ConnectionFactory) lookup("jms/ConnectionFactory");
    }

    private Destination destination(String name) throws NamingException {
        return (Destination) lookup(name);
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

    private Object lookup(final String name) throws NamingException {
        return initialContext.lookup(initParameter(name));
    }

    private String initParameter(String name) {
        return servletContext.getInitParameter(name);
    }

    void close() throws Exception {
        // HC SUNT DRACONIS!
        listener.close();
        timer.close();
        manager.close();
    }
}

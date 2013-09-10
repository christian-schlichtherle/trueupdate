/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.mini;

import java.lang.IllegalStateException;
import java.net.URI;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.*;
import javax.naming.*;
import javax.servlet.*;
import javax.servlet.annotation.WebListener;
import net.java.trueupdate.jaxrs.client.UpdateClient;
import net.java.trueupdate.manager.spec.UpdateInstaller;

/**
 * @author Christian Schlichtherle
 */
@WebListener
public final class UpdateManagerServletContextListener
implements ServletContextListener {

    private static final Logger logger = Logger.getLogger(
            UpdateManagerServletContextListener.class.getName());

    private UpdateManagerPollingDaemon pollingDaemon;
    private UpdateManagerMessageListener messageListener;

    @Override public void contextInitialized(final ServletContextEvent sce) {

        class Reactor {

            final InitialContext ic;
            final ServletContext sc = sce.getServletContext();

            {
                try {
                    ic = new InitialContext();
                } catch (NamingException ex) {
                    throw new IllegalStateException(ex);
                }
            }

            UpdateManagerPollingDaemon pollingDaemon() {
                return new UpdateManagerPollingDaemon(
                        connection(),
                        (Destination) lookup("jms/TrueUpdate Agent"),
                        updateClient(),
                        updateInstaller(),
                        checkUpdatesIntervalMinutes());
            }

            UpdateManagerMessageListener messageListener() {
                return new UpdateManagerMessageListener(
                        connection(),
                        (Destination) lookup("jms/TrueUpdate Manager"),
                        pollingDaemon);
            }

            Connection connection() {
                try {
                    return ((ConnectionFactory) lookup("jms/ConnectionFactory"))
                            .createConnection();
                } catch (JMSException ex) {
                    throw new IllegalStateException(ex);
                }
            }

            UpdateClient updateClient() {
                return new UpdateClient(updateServiceBaseUri());
            }

            URI updateServiceBaseUri() {
                final URI usburi = URI.create(initParameter("updateServiceBaseUri"));
                logger.log(Level.CONFIG, "Base URI of the update service is {0} .", usburi);
                return usburi;
            }

            int checkUpdatesIntervalMinutes() {
                final int cuim = Integer.parseInt(initParameter("checkUpdatesIntervalMinutes"));
                logger.log(Level.CONFIG, "Interval for checking for updates is {0} minutes.", cuim);
                return cuim;
            }

            UpdateInstaller updateInstaller() {
                final UpdateInstaller ui = ServiceLoader.load(
                        UpdateInstaller.class,
                        Thread.currentThread().getContextClassLoader()
                        ).iterator().next();
                logger.log(Level.CONFIG, "UpdateInstaller class is {0} .", ui.getClass());
                return ui;
            }

            Object lookup(String name) {
                try {
                    return ic.lookup(initParameter(name));
                } catch (NamingException ex) {
                    throw new IllegalStateException(ex);
                }
            }

            String initParameter(String name) {
                return sc.getInitParameter(name);
            }
        }

        final Reactor reactor = new Reactor();
        pollingDaemon = reactor.pollingDaemon();
        messageListener = reactor.messageListener();
        new Thread(pollingDaemon, "TrueUpdate Manager Polling Daemon") {
            { super.setDaemon(true); }
        }.start();
        new Thread(messageListener, "TrueUpdate Manager Message Listener Daemon") {
            { super.setDaemon(true); }
        }.start();
    }

    @Override public void contextDestroyed(ServletContextEvent sce) {
        try {
            try { messageListener.close(); }
            finally { pollingDaemon.close(); }
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }
}

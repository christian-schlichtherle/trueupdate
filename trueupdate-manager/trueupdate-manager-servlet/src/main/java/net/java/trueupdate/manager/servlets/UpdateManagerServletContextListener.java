/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.servlets;

import java.util.logging.*;
import javax.servlet.*;
import javax.servlet.annotation.WebListener;
import net.java.trueupdate.manager.jms.JmsUpdateManagerContext;

/**
 * Starts and stops the update manager.
 *
 * @author Christian Schlichtherle
 */
@WebListener
public final class UpdateManagerServletContextListener
implements ServletContextListener {

    private static final Logger logger = Logger.getLogger(
            UpdateManagerServletContextListener.class.getName());

    private JmsUpdateManagerContext context;

    @Override public void contextInitialized(final ServletContextEvent sce) {
        if (null != context) return;
        context = new JmsUpdateManagerContext();
        try {
            context.start();
        } catch (Exception ex) {
            throw new IllegalStateException(
                    "Failed to start the update manager.", ex);
        }
        logger.log(Level.CONFIG,
                "The base URI of the update service is {0} .",
                context.updateServiceBaseUri());
        logger.log(Level.CONFIG,
                "The interval for checking for artifact updates is {0} minutes.",
                context.checkUpdatesIntervalMinutes());
        logger.log(Level.CONFIG,
                "The canonical class name of the update installer is {0} .",
                context.updateInstaller().getClass().getCanonicalName());
    }

    @Override public void contextDestroyed(final ServletContextEvent sce) {
        if (null == context) return;
        try {
            context.stop();
        } catch (Exception ex) {
            throw new IllegalStateException(
                    "Failed to stop the update manager.", ex);
        }
    }
}

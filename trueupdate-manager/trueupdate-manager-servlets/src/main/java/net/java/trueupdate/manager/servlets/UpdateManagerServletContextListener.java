/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.servlets;

import java.util.logging.*;
import javax.servlet.*;
import javax.servlet.annotation.WebListener;
import javax.xml.bind.*;
import net.java.trueupdate.manager.servlets.ci.UpdateManagerCi;

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

    private static final String CONFIGURATION = "META-INF/update/manager.xml";

    private UpdateManagerContext context;

    @Override public void contextInitialized(final ServletContextEvent sce) {
        if (null != context) return;
        try {
            context = new UpdateManagerContext(parameters());
        } catch (Exception ex) {
            throw new IllegalStateException(String.format(
                    "Failed to load configuration from %s .", CONFIGURATION),
                    ex);
        }
        context.start();
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
            throw new IllegalStateException("Failed to stop the update manager context.", ex);
        }
    }

    private static UpdateManagerParameters parameters() throws Exception {
        return UpdateManagerParameters.builder().parse(configuration()).build();
    }

    private static UpdateManagerCi configuration() throws Exception {
        return (UpdateManagerCi) JAXBContext
                .newInstance(UpdateManagerCi.class)
                .createUnmarshaller()
                .unmarshal(Thread
                    .currentThread()
                    .getContextClassLoader()
                    .getResource(CONFIGURATION));
    }
}

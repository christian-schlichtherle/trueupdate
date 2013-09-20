/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.servlets;

import java.util.logging.*;
import javax.servlet.*;
import javax.servlet.annotation.WebListener;
import javax.xml.bind.*;
import net.java.trueupdate.core.codec.JaxbCodec;
import net.java.trueupdate.core.io.*;
import net.java.trueupdate.manager.servlets.config.UpdateManagerConfiguration;

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
        } catch (RuntimeException ex) {
            throw ex;
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
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    private static UpdateManagerParameters parameters() throws Exception {
        return UpdateManagerParameters.builder().parse(configuration()).build();
    }

    private static UpdateManagerConfiguration configuration() throws Exception {
        return decodeFromXml(Sources.forResource(CONFIGURATION,
                Thread.currentThread().getContextClassLoader()));
    }

    private static UpdateManagerConfiguration decodeFromXml(Source source)
    throws Exception {
        return new JaxbCodec(Lazy.JAXB_CONTEXT)
                .decode(source, UpdateManagerConfiguration.class);
    }

    private static class Lazy {

        static final JAXBContext JAXB_CONTEXT;

        static {
            try {
                JAXB_CONTEXT = JAXBContext
                        .newInstance(UpdateManagerConfiguration.class);
            } catch (JAXBException ex) {
                throw new AssertionError(ex);
            }
        }
    } // Lazy
}

/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.installer.tomcat;

import java.util.logging.Logger;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/**
 * @author Christian Schlichtherle
 */
@WebListener
public class GoodbyeWorld implements ServletContextListener {

    private static final Logger
            logger = Logger.getLogger(HelloWorld.class.getName());

    @Override public void contextInitialized(ServletContextEvent sce) { }

    @Override public void contextDestroyed(ServletContextEvent sce) {
        logger.info("Goodbye, world!");
    }
}

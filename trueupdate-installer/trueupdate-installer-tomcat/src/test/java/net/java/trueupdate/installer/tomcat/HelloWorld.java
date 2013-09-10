/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.installer.tomcat;

import java.util.logging.Logger;
import javax.servlet.*;
import javax.servlet.annotation.WebListener;

/**
 * @author Christian Schlichtherle
 */
@WebListener
public class HelloWorld implements ServletContextListener {

    private static final Logger
            logger = Logger.getLogger(HelloWorld.class.getName());

    @Override public void contextInitialized(ServletContextEvent sce) {
        logger.info("Hello, world!");
    }

    @Override public void contextDestroyed(ServletContextEvent sce) { }
}

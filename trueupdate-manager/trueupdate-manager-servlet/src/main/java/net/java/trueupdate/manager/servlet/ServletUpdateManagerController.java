/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.servlet;

import java.util.concurrent.TimeUnit;
import javax.servlet.*;
import javax.servlet.annotation.WebListener;
import net.java.trueupdate.manager.core.UpdateManagerController;
import net.java.trueupdate.util.Services;

/**
 * Loads, starts and stops an update manager controller
 * in a Servlet environment.
 *
 * @author Christian Schlichtherle
 */
@WebListener
public final class ServletUpdateManagerController
implements ServletContextListener {

    private final UpdateManagerController
            controller = Services.load(UpdateManagerController.class);

    @Override public void contextInitialized(ServletContextEvent sce) {
        controller.start();
    }

    @Override public void contextDestroyed(ServletContextEvent sce) {
        controller.stop(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
    }
}

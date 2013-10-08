/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.agent.servlet;

import java.util.concurrent.TimeUnit;
import javax.servlet.*;
import javax.servlet.annotation.WebListener;
import net.java.trueupdate.agent.core.UpdateAgentController;
import net.java.trueupdate.util.Services;

/**
 * Loads, starts and stops an update agent controller
 * in a Servlet environment.
 *
 * @author Christian Schlichtherle
 */
@WebListener
public final class ServletUpdateAgentController
implements ServletContextListener {

    private final UpdateAgentController
            controller = Services.load(UpdateAgentController.class);

    @Override public void contextInitialized(ServletContextEvent sce) {
        controller.start();
    }

    @Override public void contextDestroyed(ServletContextEvent sce) {
        controller.stop(10, TimeUnit.SECONDS);
    }
}

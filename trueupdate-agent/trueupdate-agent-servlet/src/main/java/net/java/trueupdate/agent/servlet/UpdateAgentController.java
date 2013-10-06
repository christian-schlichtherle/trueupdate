/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.agent.servlet;

import java.util.concurrent.TimeUnit;
import javax.servlet.*;
import javax.servlet.annotation.WebListener;
import net.java.trueupdate.agent.jms.*;

/**
 * Starts and stops the update agent.
 *
 * @author Christian Schlichtherle
 */
@WebListener
public final class UpdateAgentController
implements ServletContextListener {

    private JmsUpdateAgentContext context;

    @Override public void contextInitialized(ServletContextEvent sce) {
        if (null != context) return;
        context = new JmsUpdateAgentContext();
        try {
            context.start();
        } catch (Exception ex) {
            throw new IllegalStateException(
                    "Failed to start the update agent.", ex);
        }
    }

    @Override public void contextDestroyed(ServletContextEvent sce) {
        if (null == context) return;
        try {
            context.stop(10, TimeUnit.SECONDS);
        } catch (Exception ex) {
            throw new IllegalStateException(
                    "Failed to stop the update agent.", ex);
        }
    }
}

/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.agent.servlets;

import javax.servlet.*;
import javax.servlet.annotation.WebListener;
import net.java.trueupdate.agent.jms.*;
import net.java.trueupdate.agent.spec.*;

/**
 * Starts and stops the update agent.
 *
 * @author Christian Schlichtherle
 */
@WebListener
public final class UpdateAgentServletContextListener
implements ServletContextListener {

    private JmsUpdateAgentContext context;

    @Override public void contextInitialized(ServletContextEvent sce) {
        if (null != context) return;
        context = new JmsUpdateAgentContext();
        try {
            context.start();
        } catch (UpdateAgentException ex) {
            throw new IllegalStateException(
                    "Failed to start the update agent.", ex);
        }
    }

    @Override public void contextDestroyed(ServletContextEvent sce) {
        if (null == context) return;
        try {
            context.stop();
        } catch (UpdateAgentException ex) {
            throw new IllegalStateException(
                    "Failed to stop the update agent.", ex);
        }
    }
}

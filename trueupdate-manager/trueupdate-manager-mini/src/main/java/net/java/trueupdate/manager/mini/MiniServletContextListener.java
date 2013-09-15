/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.mini;

import javax.jms.JMSException;
import javax.naming.NamingException;
import javax.servlet.*;
import javax.servlet.annotation.WebListener;

/**
 * @author Christian Schlichtherle
 */
@WebListener
public final class MiniServletContextListener
implements ServletContextListener {

    private MiniContext context;

    @Override public void contextInitialized(final ServletContextEvent sce) {
        try {
            context = new MiniContext(sce.getServletContext());
        } catch (NamingException ex) {
            throw new IllegalStateException(ex);
        } catch (JMSException ex) {
            throw new IllegalStateException(ex);
        }
        context.start();
    }

    @Override public void contextDestroyed(ServletContextEvent sce) {
        try {
            context.stop();
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }
}

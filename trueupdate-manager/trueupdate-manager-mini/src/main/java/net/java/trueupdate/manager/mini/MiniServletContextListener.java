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

    private MiniReactor reactor;

    @Override public void contextInitialized(final ServletContextEvent sce) {
        try {
            reactor = new MiniReactor(sce.getServletContext());
        } catch (NamingException ex) {
            throw new IllegalStateException(ex);
        } catch (JMSException ex) {
            throw new IllegalStateException(ex);
        }
        reactor.timer().start();
        reactor.listener().start();
    }

    @Override public void contextDestroyed(ServletContextEvent sce) {
        try {
            reactor.close();
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }
}

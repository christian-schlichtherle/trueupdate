/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.agent.mini;

import javax.servlet.*;
import javax.servlet.annotation.WebListener;
import javax.xml.bind.JAXBContext;
import net.java.trueupdate.agent.mini.config.UpdateAgentConfiguration;
import net.java.trueupdate.agent.spec.UpdateAgent;

/**
 * Starts and stops the update agent.
 *
 * @author Christian Schlichtherle
 */
@WebListener
public final class UpdateAgentServletContextListener
implements ServletContextListener {

    private static final String CONFIGURATION = "META-INF/update/agent.xml";

    private UpdateAgent agent;

    @Override public void contextInitialized(ServletContextEvent sce) {
        if (null != agent) return;
        try {
            agent = new ConfiguredUpdateAgent(parameters());
            agent.subscribe();
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException(String.format(
                    "Failed to load configuration from %s .", CONFIGURATION),
                    ex);
        }
    }

    @Override public void contextDestroyed(ServletContextEvent sce) {
        if (null == agent) return;
        try {
            agent.unsubscribe();
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    private static UpdateAgentParameters parameters() throws Exception {
        return UpdateAgentParameters.builder().parse(configuration()).build();
    }

    private static UpdateAgentConfiguration configuration() throws Exception {
        return (UpdateAgentConfiguration) JAXBContext
                .newInstance(UpdateAgentConfiguration.class)
                .createUnmarshaller()
                .unmarshal(Thread
                    .currentThread()
                    .getContextClassLoader()
                    .getResource(CONFIGURATION));
    }
}

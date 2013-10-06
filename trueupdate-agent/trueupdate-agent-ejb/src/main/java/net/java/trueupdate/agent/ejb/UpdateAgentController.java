/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.agent.ejb;

import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.*;
import net.java.trueupdate.agent.jms.*;

/**
 * Starts and stops the update agent.
 *
 * @author Christian Schlichtherle
 */
@Startup
@Singleton
public class UpdateAgentController {

    private JmsUpdateAgentContext context;

    @PostConstruct void postConstruct() {
        if (null != context) return;
        context = new JmsUpdateAgentContext();
        try {
            context.start();
        } catch (Exception ex) {
            throw new IllegalStateException(
                    "Failed to start the update agent.", ex);
        }
    }

    @PreDestroy void preDestroy() {
        if (null == context) return;
        try {
            context.stop(10, TimeUnit.SECONDS);
        } catch (Exception ex) {
            throw new IllegalStateException(
                    "Failed to stop the update agent.", ex);
        }
    }
}

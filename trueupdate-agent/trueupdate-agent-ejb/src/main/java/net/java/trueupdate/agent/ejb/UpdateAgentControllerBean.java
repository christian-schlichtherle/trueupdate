/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.agent.ejb;

import java.util.concurrent.TimeUnit;
import javax.annotation.*;
import javax.ejb.*;
import net.java.trueupdate.agent.core.UpdateAgentController;
import net.java.trueupdate.util.Services;

/**
 * Loads, starts and stops an update agent controller
 * in an EJB environment.
 *
 * @author Christian Schlichtherle
 */
@Startup
@Singleton
public class UpdateAgentControllerBean {

    private UpdateAgentController controller;

    @PostConstruct void postConstruct() {
        if (null != controller) return;
        controller = Services.load(UpdateAgentController.class);
        controller.start();
    }

    @PreDestroy void preDestroy() {
        if (null == controller) return;
        controller.stop(10, TimeUnit.SECONDS);
        controller = null;
    }
}

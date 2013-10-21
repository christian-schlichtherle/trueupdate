/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.ejb;

import java.util.concurrent.TimeUnit;
import javax.annotation.*;
import javax.ejb.*;
import net.java.trueupdate.manager.core.UpdateManagerController;
import net.java.trueupdate.util.Services;

/**
 * Loads, starts and stops an update manager controller
 * in an EJB environment.
 *
 * @author Christian Schlichtherle
 */
@Startup
@Singleton
public class UpdateManagerControllerBean {

    private UpdateManagerController controller;

    @PostConstruct void postConstruct() {
        if (null != controller) return;
        controller = Services.load(UpdateManagerController.class);
        controller.start();
    }

    @PreDestroy void preDestroy() {
        if (null == controller) return;
        controller.stop(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        controller = null;
    }
}

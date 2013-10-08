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
public class EjbUpdateManagerController {

    private final UpdateManagerController
            controller = Services.load(UpdateManagerController.class);

    @PostConstruct void postConstruct() {
        controller.start();
    }

    @PreDestroy void preDestroy() {
        controller.stop(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
    }
}

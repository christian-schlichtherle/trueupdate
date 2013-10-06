/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.ejb;

import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.logging.*;
import javax.annotation.*;
import javax.ejb.*;
import net.java.trueupdate.manager.core.*;
import net.java.trueupdate.manager.jms.*;

/**
 * Starts and stops the update manager.
 *
 * @author Christian Schlichtherle
 */
@Startup
@Singleton
public class UpdateManagerController {

    private static final Logger logger = Logger.getLogger(
            UpdateManagerController.class.getName());

    private JmsUpdateManagerContext context;

    @PostConstruct void postConstruct() {
        if (null != context) return;
        context = new JmsUpdateManagerContext();
        final JmsUpdateManagerParameters ump = context.parameters();
        final UpdateServiceParameters usp = ump.updateService();
        logger.log(Level.CONFIG,
                "The base URI of the update service is {0} .", usp.uri());
        final TimerParameters tp = ump.updateTimer();
        logger.log(Level.CONFIG,
                "The delay / period for checking for artifact updates is {0} / {1} {2}.",
                new Object[] { tp.delay(), tp.period(),
                    tp.unit().name().toLowerCase(Locale.ENGLISH) });
        try {
            context.start();
        } catch (Exception ex) {
            throw new IllegalStateException(
                    "Failed to start the update manager.", ex);
        }
    }

    @PreDestroy void preDestroy() {
        if (null == context) return;
        try {
            context.stop(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        } catch (Exception ex) {
            throw new IllegalStateException(
                    "Failed to stop the update manager.", ex);
        }
    }
}

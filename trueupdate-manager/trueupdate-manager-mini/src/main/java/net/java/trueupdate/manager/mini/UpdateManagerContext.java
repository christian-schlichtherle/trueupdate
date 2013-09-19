/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.mini;

import java.net.URI;
import java.util.ServiceLoader;
import javax.annotation.concurrent.Immutable;
import javax.jms.*;
import net.java.trueupdate.jaxrs.client.UpdateClient;
import net.java.trueupdate.jms.*;
import net.java.trueupdate.manager.spec.UpdateInstaller;

/**
 * Provides the objects required for this package.
 *
 * @author Christian Schlichtherle
 */
@Immutable
final class UpdateManagerContext {

    private final UpdateManagerParameters parameters;
    private final UpdateInstaller updateInstaller;
    private final ConfiguredUpdateManager manager;
    private final UpdateTimer timer;
    private final JmsMessageReceiver receiver;

    UpdateManagerContext(final UpdateManagerParameters parameters)
    throws JMSException {
        // HC SVNT DRACONIS
        this.parameters = parameters;
        final MessagingParameters mp = parameters.messagingParameters();
        updateInstaller = ServiceLoader.load(
                UpdateInstaller.class,
                Thread.currentThread().getContextClassLoader()
                ).iterator().next();
        manager = new ConfiguredUpdateManager(
                mp.connectionFactory(),
                mp.namingContext(),
                new UpdateClient(updateServiceBaseUri()),
                updateInstaller);
        timer = new UpdateTimer(manager,
                checkUpdatesIntervalMinutes());
        receiver = JmsMessageReceiver
                .builder()
                .connectionFactory(mp.connectionFactory())
                .destination(mp.fromDestination())
                .subscriptionName(mp.fromName())
                .messageSelector("manager = true")
                .messageListener(manager)
                .build();
    }

    URI updateServiceBaseUri() { return parameters.updateServiceBaseUri(); }

    int checkUpdatesIntervalMinutes() {
        return parameters.checkUpdatesIntervalMinutes();
    }

    UpdateInstaller updateInstaller() { return updateInstaller; }

    void start() {
        timer().start();
        receiver().start();
    }

    private Thread timer() {
        return new Thread(timer, "TrueUpdate Manager Mini / Timer Daemon") {
            { super.setDaemon(true); }
        };
    }

    private Thread receiver() {
        return new Thread(receiver, "TrueUpdate Manager Mini / Receiver Daemon") {
            { super.setDaemon(true); }
        };
    }

    void stop() throws Exception {
        // HC SUNT DRACONIS!
        receiver.stop();
        timer.stop();
        manager.close();
    }
}

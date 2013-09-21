/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.jms;

import java.net.URI;
import javax.annotation.concurrent.Immutable;
import net.java.trueupdate.jms.*;
import net.java.trueupdate.manager.core.UpdateManagerException;
import net.java.trueupdate.manager.spec.UpdateInstaller;

/**
 * A context for the JMS Update Manager.
 *
 * @author Christian Schlichtherle
 */
@Immutable
public final class JmsUpdateManagerContext {

    private final JmsUpdateManagerParameters parameters;
    private final JmsUpdateManager manager;
    private final JmsUpdateTimer timer;
    private final JmsMessageReceiver receiver;

    public JmsUpdateManagerContext() {
        this(JmsUpdateManagerParameters.load());
    }

    public JmsUpdateManagerContext(final JmsUpdateManagerParameters parameters) {
        // HC SVNT DRACONIS
        this.parameters = parameters;
        manager = new JmsUpdateManager(parameters);
        final MessagingParameters mp = parameters.messagingParameters();
        receiver = JmsMessageReceiver
                .builder()
                .connectionFactory(mp.connectionFactory())
                .destination(mp.fromDestination())
                .subscriptionName(mp.fromName())
                .messageSelector("manager = true")
                .updateMessageListener(manager)
                .build();
        timer = new JmsUpdateTimer(parameters, manager);
    }

    public URI updateServiceBaseUri() {
        return parameters.updateServiceBaseUri();
    }

    public int checkUpdatesIntervalMinutes() {
        return parameters.checkUpdatesIntervalMinutes();
    }

    public UpdateInstaller updateInstaller() {
        return manager.updateInstaller();
    }

    public void start() throws UpdateManagerException {
        receiverThread().start();
        timerThread().start();
    }

    public void stop() throws UpdateManagerException {
        // HC SUNT DRACONIS!
        try {
            timer.stop();
            receiver.stop();
        } catch (Exception ex) {
            throw new UpdateManagerException(ex);
        }
        manager.close();
    }

    private Thread timerThread() {
        return new Thread(timer, "TrueUpdate Manager / Timer Daemon") {
            { super.setDaemon(true); }
        };
    }

    private Thread receiverThread() {
        return new Thread(receiver, "TrueUpdate Manager / Receiver Daemon") {
            { super.setDaemon(true); }
        };
    }
}

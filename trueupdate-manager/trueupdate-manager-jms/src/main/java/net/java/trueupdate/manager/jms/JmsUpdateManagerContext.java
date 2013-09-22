/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.jms;

import javax.annotation.concurrent.Immutable;
import net.java.trueupdate.jms.*;
import net.java.trueupdate.manager.core.UpdateTimer;

/**
 * A context for the JMS Update Manager.
 *
 * @author Christian Schlichtherle
 */
@Immutable
public final class JmsUpdateManagerContext {

    private final JmsUpdateManagerParameters parameters;
    private final JmsUpdateManager manager;
    private final UpdateTimer timer;
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
                .messageSelector("Manager = true")
                .updateMessageListener(manager)
                .build();
        timer = new UpdateTimer(manager,
                parameters.checkUpdatesIntervalMinutes());
    }

    public JmsUpdateManagerParameters parameters() { return parameters; }

    public void start() throws Exception {
        receiverThread().start();
        timerThread().start();
    }

    public void stop() throws Exception {
        // HC SUNT DRACONIS!
        timer.stop();
        receiver.stop();
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

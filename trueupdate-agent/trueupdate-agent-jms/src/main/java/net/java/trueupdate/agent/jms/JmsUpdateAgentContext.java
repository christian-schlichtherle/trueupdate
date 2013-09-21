/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.agent.jms;

import javax.annotation.concurrent.Immutable;
import javax.jms.JMSException;
import net.java.trueupdate.agent.spec.UpdateAgentException;
import net.java.trueupdate.jms.JmsMessageReceiver;
import net.java.trueupdate.jms.MessagingParameters;

/**
 * A context for the JMS Update Agent.
 *
 * @author Christian Schlichtherle
 */
@Immutable
public final class JmsUpdateAgentContext {

    private final JmsUpdateAgent agent;
    private final JmsMessageReceiver receiver;

    public JmsUpdateAgentContext() {
        this(JmsUpdateAgentParameters.load());
    }

    public JmsUpdateAgentContext(final JmsUpdateAgentParameters parameters) {
        agent = new JmsUpdateAgent(parameters);
        final MessagingParameters mp = parameters.messagingParameters();
        receiver = JmsMessageReceiver
                .builder()
                .connectionFactory(mp.connectionFactory())
                .destination(mp.fromDestination())
                .subscriptionName(mp.fromName())
                .messageSelector("manager = false")
                .updateMessageListener(agent)
                .build();
    }

    public void start() throws UpdateAgentException {
        receiverThread().start();
        agent.subscribe();
    }

    public void stop() throws UpdateAgentException {
        // HC SVNT DRACONIS
        try {
            receiver.stop();
        } catch (JMSException ex) {
            throw new UpdateAgentException(ex);
        }
        agent.unsubscribe();
    }

    private Thread receiverThread() {
        return new Thread(receiver, "TrueUpdate Agent / Receiver Daemon") {
            { super.setDaemon(true); }
        };
    }
}

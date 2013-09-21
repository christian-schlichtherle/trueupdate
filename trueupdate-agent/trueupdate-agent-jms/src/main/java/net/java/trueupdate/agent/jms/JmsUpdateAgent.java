/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.agent.jms;

import javax.annotation.concurrent.Immutable;
import javax.jms.*;
import javax.naming.*;
import net.java.trueupdate.agent.core.BasicUpdateAgent;
import net.java.trueupdate.agent.spec.*;
import net.java.trueupdate.jms.*;
import net.java.trueupdate.message.UpdateMessage;

/**
 * An implementation of the update agent which depends only on JMS.
 *
 * @author Christian Schlichtherle
 */
@Immutable
final class JmsUpdateAgent extends BasicUpdateAgent {

    private final ApplicationParameters applicationParameters;
    private final MessagingParameters messagingParameters;

    JmsUpdateAgent(final JmsUpdateAgentParameters parameters) {
        this.applicationParameters = parameters.applicationParameters();
        this.messagingParameters = parameters.messagingParameters();
    }

    @Override
    protected void send(UpdateMessage message) throws Exception {
        JmsMessageSender.create(namingContext(), connectionFactory())
                .send(message);
    }

    private Context namingContext() {
        return messagingParameters.namingContext();
    }

    private ConnectionFactory connectionFactory() {
        return messagingParameters.connectionFactory();
    }

    @Override protected String from() {
        return messagingParameters.fromName();
    }

    @Override protected String to() {
        return messagingParameters.toName();
    }

    @Override protected ApplicationParameters applicationParameters() {
        return applicationParameters;
    }
}

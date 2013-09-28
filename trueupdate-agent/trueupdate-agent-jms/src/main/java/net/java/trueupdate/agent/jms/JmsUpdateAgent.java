/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.agent.jms;

import javax.annotation.concurrent.Immutable;
import javax.jms.*;
import javax.naming.*;
import net.java.trueupdate.agent.core.*;
import net.java.trueupdate.jms.*;
import net.java.trueupdate.message.UpdateMessage;

/**
 * An implementation of the update agent which depends only on JMS.
 *
 * @author Christian Schlichtherle
 */
@Immutable
final class JmsUpdateAgent extends CoreUpdateAgent {

    private final ApplicationParameters applicationParameters;
    private final MessagingParameters messagingParameters;

    JmsUpdateAgent(final JmsUpdateAgentParameters parameters) {
        this.applicationParameters = parameters.application();
        this.messagingParameters = parameters.messaging();
    }

    @Override
    protected void send(UpdateMessage message) throws Exception {
        JmsSender.create(namingContext(), connectionFactory()).send(message);
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

/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.agent.jms;

import javax.annotation.concurrent.Immutable;
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
    private final JmsParameters jmsParameters;
    private final JmsSender sender;

    JmsUpdateAgent(final JmsUpdateAgentParameters parameters) {
        this.applicationParameters = parameters.application();
        this.jmsParameters = parameters.messaging();
        this.sender = JmsSender.create(
                this.jmsParameters.namingContext(),
                this.jmsParameters.connectionFactory());
    }

    @Override
    protected void send(UpdateMessage message) throws Exception {
        sender.send(message);
    }

    @Override protected String from() {
        return jmsParameters.fromName();
    }

    @Override protected String to() {
        return jmsParameters.toName();
    }

    @Override protected ApplicationParameters applicationParameters() {
        return applicationParameters;
    }
}

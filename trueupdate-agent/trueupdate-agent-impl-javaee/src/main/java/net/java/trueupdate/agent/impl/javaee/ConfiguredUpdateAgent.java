/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.agent.impl.javaee;

import static java.util.Objects.requireNonNull;
import javax.jms.*;
import net.java.trueupdate.agent.core.*;
import net.java.trueupdate.agent.spec.*;
import net.java.trueupdate.manager.spec.UpdateMessage;

/**
 * A configured update agent.
 *
 * @author Christian Schlichtherle
 */
final class ConfiguredUpdateAgent extends BasicUpdateAgent {

    private final ApplicationParameters applicationParameters;
    private final ConnectionFactory connectionFactory;
    private final Destination destination;
    private final UpdateMessageDispatcher updateMessageDispatcher;

    ConfiguredUpdateAgent(final ApplicationParameters applicationParameters,
                          final UpdateAgentBuilderBean b) {
        this.applicationParameters = requireNonNull(applicationParameters);
        this.connectionFactory = requireNonNull(b.connectionFactory);
        this.destination = requireNonNull(b.destination);
        this.updateMessageDispatcher = requireNonNull(b.updateAgentMessageDispatcher);
    }

    @Override protected UpdateMessageDispatcher updateMessageDispatcher() {
        return updateMessageDispatcher;
    }

    @Override protected ApplicationParameters applicationParameters() {
        return applicationParameters;
    }

    @Override
    protected UpdateMessage send(final UpdateMessage message) throws Exception {
        final Connection c = connectionFactory.createConnection();
        try {
            final Session s = c.createSession(false, Session.AUTO_ACKNOWLEDGE);
            final Message m = s.createObjectMessage(message);
            m.setBooleanProperty("manager", message.type().forManager());
            s.createProducer(destination).send(m);
        } finally {
            c.close();
        }
        return message;
    }
}

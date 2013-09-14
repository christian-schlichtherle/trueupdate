/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.agent.javaee;

import java.net.URI;
import javax.jms.*;
import net.java.trueupdate.agent.core.*;
import net.java.trueupdate.agent.spec.*;
import net.java.trueupdate.manager.spec.UpdateMessage;
import static net.java.trueupdate.util.Objects.*;

/**
 * A configured update agent.
 *
 * @author Christian Schlichtherle
 */
final class ConfiguredUpdateAgent extends BasicUpdateAgent {

    private static final URI
            AGENT_URI = URI.create("agent"),
            MANAGER_URI = URI.create("manager");

    private final ApplicationParameters applicationParameters;
    private final ConnectionFactory connectionFactory;
    private final Destination destination;
    private final UpdateAgentMessageDispatcherBean updateAgentMessageDispatcher;

    ConfiguredUpdateAgent(final ApplicationParameters applicationParameters,
                          final UpdateAgentBuilderBean b) {
        this.applicationParameters = requireNonNull(applicationParameters);
        this.connectionFactory = requireNonNull(b.connectionFactory);
        this.destination = requireNonNull(b.destination);
        this.updateAgentMessageDispatcher = requireNonNull(b.updateAgentMessageDispatcher);
    }

    @Override
    protected ApplicationParameters applicationParameters() {
        return applicationParameters;
    }

    @Override protected URI from() { return AGENT_URI; }

    @Override protected URI to() { return MANAGER_URI; }

    @Override
    public void subscribe() throws UpdateAgentException {
        updateAgentMessageDispatcher.subscribe(applicationParameters);
        super.subscribe();
    }

    @Override
    public void unsubscribe() throws UpdateAgentException {
        super.unsubscribe();
        updateAgentMessageDispatcher.unsubscribe(applicationParameters);
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

/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.agent.javaee;

import net.java.trueupdate.manager.api.UpdateMessage;
import net.java.trueupdate.agent.api.ApplicationParameters;
import net.java.trueupdate.agent.api.UpdateAgentException;
import static java.util.Objects.requireNonNull;
import java.util.concurrent.Callable;
import javax.jms.*;
import net.java.trueupdate.agent.core.BasicUpdateAgent;

/**
 * A configured update agent.
 *
 * @author Christian Schlichtherle
 */
final class ConfiguredUpdateAgent extends BasicUpdateAgent {

    private final ApplicationParameters applicationParameters;
    private final ConnectionFactory connectionFactory;
    private final Destination destination;
    private final UpdateAgentDispatcherBean updateAgentDispatcher;

    ConfiguredUpdateAgent(final ApplicationParameters applicationParameters,
                          final UpdateAgentBuilderBean b) {
        this.applicationParameters = requireNonNull(applicationParameters);
        this.connectionFactory = requireNonNull(b.connectionFactory);
        this.destination = requireNonNull(b.destination);
        this.updateAgentDispatcher = requireNonNull(b.updateAgentDispatcher);
    }

    @Override protected ApplicationParameters applicationParameters() {
        return applicationParameters;
    }

    @Override public void subscribe() throws UpdateAgentException {
        super.subscribe();
        updateAgentDispatcher.subscribe(applicationParameters());
    }

    @Override public void unsubscribe() throws UpdateAgentException {
        updateAgentDispatcher.unsubscribe(applicationParameters());
        super.unsubscribe();
    }

    @Override
    protected UpdateMessage send(final UpdateMessage message) throws Exception {
        return new MessageProducerTask<UpdateMessage>() {
            @Override
            public UpdateMessage use(MessageProducer mp, Session s, Connection c)
            throws Exception {
                final Message m = s.createObjectMessage(message);
                m.setBooleanProperty("manager", message.type().forManager());
                mp.send(m);
                return message;
            }
        }.call();
    }

    private abstract class MessageProducerTask<V> implements Callable<V> {

        @Override public final V call() throws Exception {
            final Connection c = connectionFactory.createConnection();
            try {
                //c.start();
                return use(c);
            } finally {
                c.close();
            }
        }

        private V use(Connection c) throws Exception {
            return use(c.createSession(true, 0), c);
        }

        private V use(Session s, Connection c) throws Exception {
            return use(s.createProducer(destination), s, c);
        }

        abstract V use(MessageProducer mp, Session s, Connection c)
        throws Exception;
    }
}

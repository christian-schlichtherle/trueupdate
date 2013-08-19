/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.agent.impl.javaee;

import static java.util.Objects.requireNonNull;
import java.util.concurrent.Callable;
import javax.jms.*;
import net.java.trueupdate.agent.impl.core.BasicUpdateAgent;
import net.java.trueupdate.agent.spec.*;
import net.java.trueupdate.manager.spec.*;

/**
 * A configured update agent.
 *
 * @author Christian Schlichtherle
 */
final class ConfiguredUpdateAgent extends BasicUpdateAgent {

    private final ApplicationParameters applicationParameters;
    private final ConnectionFactory connectionFactory;
    private final Destination destination;

    ConfiguredUpdateAgent(final ApplicationParameters applicationParameters,
                          final UpdateAgentBuilderBean b) {
        this.applicationParameters = requireNonNull(applicationParameters);
        this.connectionFactory = requireNonNull(b.connectionFactory);
        this.destination = requireNonNull(b.destination);
    }

    @Override protected ApplicationParameters applicationParameters() {
        return applicationParameters;
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

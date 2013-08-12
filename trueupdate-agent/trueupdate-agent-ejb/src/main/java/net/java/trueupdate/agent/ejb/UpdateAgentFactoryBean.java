/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.agent.ejb;

import java.net.URI;
import java.util.Objects;
import java.util.concurrent.Callable;
import javax.annotation.Nullable;
import javax.annotation.Resource;
import javax.ejb.Local;
import javax.ejb.Singleton;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import net.java.trueupdate.agent.spec.UpdateAgent;
import net.java.trueupdate.agent.spec.UpdateAgentFactory;
import net.java.trueupdate.agent.spec.UpdateManagerListener;
import net.java.trueupdate.artifact.spec.ArtifactDescriptor;
import net.java.trueupdate.message.UpdateMessageException;
import net.java.trueupdate.message.UpdateMessage;
import net.java.trueupdate.message.UpdateMessage.Type;
import static net.java.trueupdate.message.UpdateMessage.Type.INSTALLATION_REQUEST;
import static net.java.trueupdate.message.UpdateMessage.Type.SUBSCRIPTION_REQUEST;
import static net.java.trueupdate.message.UpdateMessage.Type.UNSUBSCRIPTION_REQUEST;

/**
 * @author Christian Schlichtherle
 */
@Singleton
@Local(UpdateAgentFactory.class)
public class UpdateAgentFactoryBean
implements UpdateAgentFactory {

    private static final String JNDI_NAME = "jms/trueupdate";

    private static final URI DESTINATION_URI = URI.create(JNDI_NAME);

    @Resource
    private ConnectionFactory connectionFactory;

    @Resource(lookup = JNDI_NAME)
    private Destination destination;

    @Override
    public UpdateAgent newUpdateAgent(ArtifactDescriptor artifactDescriptor,
                                      UpdateManagerListener listener) {
        return new DefaultUpdateAgent(artifactDescriptor);
    }

    private class DefaultUpdateAgent implements UpdateAgent {

        private final ArtifactDescriptor artifactDescriptor;

        DefaultUpdateAgent(final ArtifactDescriptor artifactDescriptor) {
            this.artifactDescriptor = Objects.requireNonNull(artifactDescriptor);
        }

        @Override public void subscribe() throws UpdateMessageException {
            send(SUBSCRIPTION_REQUEST);
        }

        @Override public void install(String version) throws UpdateMessageException {
            send(INSTALLATION_REQUEST);
        }

        @Override public void unsubscribe() throws UpdateMessageException {
            send(UNSUBSCRIPTION_REQUEST);
        }

        private void send(final Type type) throws UpdateMessageException {
            wrap(new MessageProducerTask<Void>() {
                @Override
                public Void use(MessageProducer mp, Session s, Connection c)
                throws Exception {
                    mp.send(s.createObjectMessage(
                            UpdateMessage
                                .create()
                                .from(DESTINATION_URI)
                                .to(DESTINATION_URI)
                                .artifactDescriptor(artifactDescriptor)
                                .type(type)
                                .build()));
                    return null;
                }
            });
        }

        private @Nullable <V> V wrap(final Callable<V> task) throws UpdateMessageException {
            try {
                return task.call();
            } catch (RuntimeException ex) {
                throw ex;
            } catch (Exception ex) {
                throw new UpdateMessageException(ex);
            }
        }
    } // DefaultUpdateAgent

    private abstract class MessageProducerTask<V> implements Callable<V> {

        @Override public final V call() throws Exception {
            return new SessionTask<V>() {
                @Override public V use(final Session s, final Connection c)
                throws Exception {
                    final MessageProducer mp = s.createProducer(destination);
                    return MessageProducerTask.this.use(mp, s, c);
                }
            }.call();
        }

        protected abstract V use(MessageProducer mp, Session s, Connection c)
        throws Exception;
    }

    private abstract class MessageConsumerTask<V> implements Callable<V> {

        @Override public final V call() throws Exception {
            return new SessionTask<V>() {
                @Override public V use(final Session s, final Connection c)
                throws Exception {
                    final MessageConsumer mc = s.createConsumer(destination);
                    return MessageConsumerTask.this.use(mc, s, c);
                }
            }.call();
        }

        protected abstract V use(MessageConsumer mc, Session s, Connection c)
        throws Exception;
    }

    private abstract class SessionTask<V> implements Callable<V> {

        @Override public final V call() throws Exception {
            final Connection c = connectionFactory.createConnection();
            try {
                //c.start();
                return use(c.createSession(true, 0), c);
            } finally {
                c.close();
            }
        }

        protected abstract V use(Session s, Connection c) throws Exception;
    }
}

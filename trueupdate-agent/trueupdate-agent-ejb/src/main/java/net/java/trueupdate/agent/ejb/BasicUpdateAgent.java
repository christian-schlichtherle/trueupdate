/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.agent.ejb;

import java.net.URI;
import static java.util.Objects.requireNonNull;
import java.util.concurrent.Callable;
import javax.annotation.Nullable;
import javax.jms.*;
import net.java.trueupdate.agent.spec.*;
import net.java.trueupdate.artifact.spec.ArtifactDescriptor;
import net.java.trueupdate.manager.spec.*;
import static net.java.trueupdate.manager.spec.UpdateMessage.Type.*;

/**
 * @author Christian Schlichtherle
 */
final class BasicUpdateAgent implements UpdateAgent {

    private static final URI
            DESTINATION_URI = URI.create(MessageListenerBean.LOOKUP_NAME);

    private final ConnectionFactory connectionFactory;
    private final Destination destination;
    private final ApplicationParameters parameters;

    BasicUpdateAgent(final UpdateAgentBuilderBean b) {
        this.connectionFactory = requireNonNull(b.connectionFactory);
        this.destination = requireNonNull(b.destination);
        this.parameters = requireNonNull(b.applicationParameters);
    }

    private ApplicationDescriptor applicationDescriptor() {
        return parameters.applicationDescriptor();
    }

    private ArtifactDescriptor artifactDescriptor() {
        return applicationDescriptor().artifactDescriptor();
    }

    private URI currentLocation() {
        return applicationDescriptor().currentLocation();
    }

    private URI updateLocation() { return parameters.updateLocation(); }

    @Override public void subscribe() throws UpdateAgentException {
        send(SUBSCRIPTION_REQUEST, null);
    }

    @Override public void install(String version) throws UpdateAgentException {
        send(INSTALLATION_REQUEST, version);
    }

    @Override public void unsubscribe() throws UpdateAgentException {
        send(UNSUBSCRIPTION_REQUEST, null);
    }

    private void send(final UpdateMessage.Type type, final String updateVersion)
    throws UpdateAgentException {
        wrap(new MessageProducerTask<Void>() {
            @Override
            public Void use(MessageProducer mp, Session s, Connection c)
            throws Exception {
                final UpdateMessage um = UpdateMessage
                            .builder()
                            .from(DESTINATION_URI)
                            .to(DESTINATION_URI)
                            .type(type)
                            .artifactDescriptor(artifactDescriptor())
                            .currentLocation(currentLocation())
                            .updateVersion(updateVersion)
                            .updateLocation(updateLocation())
                            .build();
                final Message m = s.createObjectMessage(um);
                m.setBooleanProperty("request", true);
                mp.send(m);
                return null;
            }
        });
    }

    private @Nullable <V> V wrap(final Callable<V> task)
    throws UpdateAgentException {
        try { return task.call(); }
        catch (RuntimeException ex) { throw ex; }
        catch (Exception ex) { throw new UpdateAgentException(ex); }
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

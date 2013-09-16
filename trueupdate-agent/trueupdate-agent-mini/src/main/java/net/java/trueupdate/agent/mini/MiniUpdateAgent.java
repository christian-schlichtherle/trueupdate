/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.agent.mini;

import java.util.concurrent.Callable;
import javax.annotation.CheckForNull;
import javax.jms.*;
import javax.naming.*;
import net.java.trueupdate.agent.core.BasicUpdateAgent;
import net.java.trueupdate.agent.spec.ApplicationParameters;
import net.java.trueupdate.agent.spec.UpdateAgentException;
import net.java.trueupdate.jms.JmsMessageReceiver;
import net.java.trueupdate.jms.JmsMessageTransmitter;
import net.java.trueupdate.manager.spec.UpdateMessage;
import static net.java.trueupdate.util.Objects.requireNonNull;

/**
 * An implementation of the abstract update agent class with minimal
 * dependencies.
 *
 * @author Christian Schlichtherle
 */
final class MiniUpdateAgent extends BasicUpdateAgent {

    private final ApplicationParameters applicationParameters;
    private final MessagingParameters messagingParameters;
    private @CheckForNull JmsMessageReceiver receiver;

    MiniUpdateAgent(final ApplicationParameters applicationParameters,
                    final MessagingParameters messagingParameters) {
        this.applicationParameters = requireNonNull(applicationParameters);
        this.messagingParameters = requireNonNull(messagingParameters);
    }

    @Override
    public void subscribe() throws UpdateAgentException {
        startUpdateMessageListener();
        super.subscribe();
    }

    @Override
    public void unsubscribe() throws UpdateAgentException {
        super.unsubscribe();
        stopUpdateMessageListener();
    }

    private void startUpdateMessageListener() throws UpdateAgentException {
        if (null != receiver) return;
        wrap(new Callable<Void>() {
            @Override public Void call() throws NamingException, JMSException {
                receiver = JmsMessageReceiver
                        .builder()
                        .connectionFactory(connectionFactory())
                        .destination(fromDestination())
                        .subscriptionName("TrueUpdate Agent")
                        .messageSelector("manager = false")
                        .messageListener(new MiniUpdateMessageListener(applicationParameters))
                        .build();
                new Thread(receiver, "TrueUpdate Agent Mini Receiver Daemon") {
                    { super.setDaemon(true); }
                }.start();
                return null;
            }
        });
    }

    private void stopUpdateMessageListener() throws UpdateAgentException {
        if (null == receiver) return;
        wrap(new Callable<Void>() {
            @Override public Void call() throws JMSException {
                receiver.stop();
                return null;
            }
        });
    }

    private static <T> T wrap(final Callable<T> task)
    throws UpdateAgentException {
        try {
            return task.call();
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new UpdateAgentException(ex);
        }
    }

    @Override
    protected void send(UpdateMessage message) throws Exception {
        JmsMessageTransmitter.create(context(), connectionFactory())
                .send(message);
    }

    private ConnectionFactory connectionFactory() throws NamingException {
        return lookup(messagingParameters().connectionFactory());
    }

    private Destination fromDestination() throws NamingException {
        return lookup(from());
    }

    @SuppressWarnings("unchecked")
    private <T> T lookup(String name) throws NamingException {
        return (T) context().lookup(name);
    }

    private Context context() { return messagingParameters().context(); }

    @Override protected String from() { return messagingParameters().from(); }

    @Override protected String to() { return messagingParameters().to(); }

    @Override protected ApplicationParameters applicationParameters() {
        return applicationParameters;
    }

    private MessagingParameters messagingParameters() {
        return messagingParameters;
    }
}

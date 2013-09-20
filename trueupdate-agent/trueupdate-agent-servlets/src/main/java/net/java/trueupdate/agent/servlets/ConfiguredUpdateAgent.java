/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.agent.servlets;

import java.util.concurrent.Callable;
import javax.annotation.CheckForNull;
import javax.jms.*;
import javax.naming.*;
import net.java.trueupdate.agent.core.BasicUpdateAgent;
import net.java.trueupdate.agent.spec.*;
import net.java.trueupdate.jms.*;
import net.java.trueupdate.manager.spec.UpdateMessage;
import net.java.trueupdate.manager.spec.UpdateMessageListener;

/**
 * An implementation of the abstract update agent class with minimal
 * dependencies.
 *
 * @author Christian Schlichtherle
 */
final class ConfiguredUpdateAgent extends BasicUpdateAgent {

    private final ApplicationParameters applicationParameters;
    private final MessagingParameters messagingParameters;
    private @CheckForNull JmsMessageReceiver receiver;

    ConfiguredUpdateAgent(final UpdateAgentParameters parameters) {
        this.applicationParameters = parameters.applicationParameters();
        this.messagingParameters = parameters.messagingParameters();
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
                        .subscriptionName(from())
                        .messageSelector("manager = false")
                        .messageListener(updateMessageListener())
                        .build();
                new Thread(receiver, "TrueUpdate Agent Mini / Receiver Daemon") {
                    { super.setDaemon(true); }
                }.start();
                return null;
            }
        });
    }

    private UpdateMessageListener updateMessageListener() {
        return new ConfiguredUpdateMessageListener(this, applicationParameters);
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
        JmsMessageSender.create(namingContext(), connectionFactory())
                .send(message);
    }

    @Override protected ApplicationParameters applicationParameters() {
        return applicationParameters;
    }

    private Context namingContext() {
        return messagingParameters().namingContext();
    }

    private ConnectionFactory connectionFactory() {
        return messagingParameters().connectionFactory();
    }

    private Destination fromDestination() {
        return messagingParameters().fromDestination();
    }

    @Override protected String from() {
        return messagingParameters().fromName();
    }

    @Override protected String to() {
        return messagingParameters().toName();
    }

    private MessagingParameters messagingParameters() {
        return messagingParameters;
    }
}

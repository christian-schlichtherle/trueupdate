/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.agent.jms;

import java.net.URL;
import java.util.concurrent.Callable;
import javax.annotation.CheckForNull;
import javax.annotation.concurrent.ThreadSafe;
import javax.jms.*;
import javax.naming.*;
import javax.xml.bind.JAXB;
import net.java.trueupdate.agent.core.BasicUpdateAgent;
import net.java.trueupdate.agent.jms.dto.JmsUpdateAgentParametersDto;
import net.java.trueupdate.agent.spec.*;
import net.java.trueupdate.jms.*;
import net.java.trueupdate.manager.spec.*;
import static net.java.trueupdate.util.Resources.locate;

/**
 * An implementation of the update agent which depends only on JMS.
 *
 * @author Christian Schlichtherle
 */
@ThreadSafe
public final class JmsUpdateAgent extends BasicUpdateAgent {

    private static final String CONFIGURATION = "META-INF/update/agent.xml";

    private final ApplicationParameters applicationParameters;
    private final MessagingParameters messagingParameters;
    private @CheckForNull JmsMessageReceiver receiver;

    public static JmsUpdateAgent load() { return load(CONFIGURATION); }

    static JmsUpdateAgent load(final String resourceName) {
        try {
            return new JmsUpdateAgent(parameters(locate(resourceName)));
        } catch (Exception ex) {
            throw new java.lang.IllegalStateException(String.format(
                    "Failed to load configuration from %s .", resourceName),
                    ex);
        }
    }

    private static JmsUpdateAgentParameters parameters(final URL source)
    throws Exception {
        return JmsUpdateAgentParameters.parse(
                JAXB.unmarshal(source, JmsUpdateAgentParametersDto.class));
    }

    public JmsUpdateAgent(final JmsUpdateAgentParameters parameters) {
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

    private synchronized void startUpdateMessageListener()
    throws UpdateAgentException {
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

    UpdateMessageListener updateMessageListener() {
        return new JmsUpdateMessageListener(this, applicationParameters);
    }

    private synchronized void stopUpdateMessageListener()
    throws UpdateAgentException {
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

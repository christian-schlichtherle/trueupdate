/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.agent.impl.javaee;

import java.io.Serializable;
import java.util.logging.*;
import javax.annotation.Resource;
import javax.ejb.*;
import javax.jms.*;
import net.java.trueupdate.agent.core.UpdateMessageDispatcher;
import net.java.trueupdate.manager.spec.UpdateMessage;

/**
 * Filters JMS messages and forwards update messages to the injected
 * {@link UpdateAgentDispatcherBean}.
 *
 * @author Christian Schlichtherle
 */
@MessageDriven(mappedName = "jms/TrueUpdate Agent",
        activationConfig = {
            @ActivationConfigProperty(propertyName = "messageSelector",
                                      propertyValue = "manager = false"),
        })
public class UpdateAgentMessageListenerBean implements MessageListener {

    private static final Logger
            logger = Logger.getLogger(UpdateAgentMessageListenerBean.class.getName());

    @EJB
    private UpdateMessageDispatcher updateMessageDispatcher;

    @Resource
    private MessageDrivenContext context;

    @Override public void onMessage(final Message message) {
        logger.log(Level.FINEST, "Received JMS message for update agent: {0}", message);
        try {
            if (message instanceof ObjectMessage) {
                final Serializable body = ((ObjectMessage) message).getObject();
                if (body instanceof UpdateMessage)
                    updateMessageDispatcher.onUpdateMessage((UpdateMessage) body);
            }
        } catch (RuntimeException ex) {
            throw ex;
        } catch (final Exception ex) {
            context.setRollbackOnly();
            logger.log(Level.SEVERE, "Could not process JMS message.", ex);
        }
    }
}
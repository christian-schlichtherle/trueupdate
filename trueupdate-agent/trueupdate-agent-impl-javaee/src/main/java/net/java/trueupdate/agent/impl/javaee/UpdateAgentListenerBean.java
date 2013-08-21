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
import net.java.trueupdate.manager.spec.UpdateMessage;

/**
 * Filters JMS messages and forwards update messages to the injected
 * {@link UpdateAgentDispatcherBean}.
 *
 * @author Christian Schlichtherle
 */
@MessageDriven(
        activationConfig = {
            @ActivationConfigProperty(propertyName = "messageSelector",
                                      propertyValue = "manager = false"),
            @ActivationConfigProperty(propertyName = "destination",
                                      propertyValue = "TrueUpdate"),
            @ActivationConfigProperty(propertyName = "destinationType",
                                      propertyValue = "javax.jms.Topic"),
        })
public class UpdateAgentListenerBean implements MessageListener {

    private static final Logger
            logger = Logger.getLogger(UpdateAgentListenerBean.class.getName());

    @EJB
    private UpdateAgentDispatcherBean updateAgentDispatcher;

    @Resource
    private MessageDrivenContext context;

    @Override public void onMessage(final Message message) {
        try {
            if (message instanceof ObjectMessage) {
                final Serializable body = ((ObjectMessage) message).getObject();
                if (body instanceof UpdateMessage)
                    updateAgentDispatcher.onUpdateMessage((UpdateMessage) body);
            }
        } catch (RuntimeException ex) {
            throw ex;
        } catch (final Exception ex) {
            context.setRollbackOnly();
            logger.log(Level.SEVERE, "Could not process message.", ex);
        }
    }
}

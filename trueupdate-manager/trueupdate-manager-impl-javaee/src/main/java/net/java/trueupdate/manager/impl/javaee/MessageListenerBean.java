/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.impl.javaee;

import java.io.Serializable;
import java.util.logging.*;
import javax.annotation.Resource;
import javax.ejb.*;
import javax.jms.*;
import net.java.trueupdate.manager.spec.*;

/**
 * Filters JMS messages and forwards update messages to the injected
 * {@link UpdateMessageListener}.
 *
 * @author Christian Schlichtherle
 */
@MessageDriven(mappedName = MessageListenerBean.DESTINATION_LOOKUP,
        activationConfig = {
            @ActivationConfigProperty(propertyName = "messageSelector",
                                      propertyValue = "manager = true"),
            @ActivationConfigProperty(propertyName = "destinationType",
                                      propertyValue = "javax.jms.Topic"),
            @ActivationConfigProperty(propertyName = "subscriptionDurability",
                                      propertyValue = "Durable"),
            @ActivationConfigProperty(propertyName = "subscriptionName",
                                      propertyValue = "TrueUpdate Manager"),
            @ActivationConfigProperty(propertyName = "clientId",
                                      propertyValue = "nevermind"),
        })
public class MessageListenerBean implements MessageListener {

    private static final Logger
            logger = Logger.getLogger(MessageListenerBean.class.getName());

    static final String DESTINATION_LOOKUP = "TrueUpdate";

    @EJB
    private UpdateMessageListener updateMessageListener;

    @Resource
    private MessageDrivenContext context;

    @Override public void onMessage(final Message message) {
        try {
            if (message instanceof ObjectMessage) {
                final Serializable body = ((ObjectMessage) message).getObject();
                if (body instanceof UpdateMessage)
                    updateMessageListener.onUpdateMessage((UpdateMessage) body);
            }
        } catch (RuntimeException ex) {
            throw ex;
        } catch (final Exception ex) {
            context.setRollbackOnly();
            logger.log(Level.SEVERE, "Could not process message.", ex);
        }
    }
}

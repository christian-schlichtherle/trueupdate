/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.agent.ejb;

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
@MessageDriven(mappedName = "jms/trueupdate",
        activationConfig = {
            @ActivationConfigProperty(propertyName = "destinationLookup",
                                      propertyValue = "jms/trueupdate"),
            @ActivationConfigProperty(propertyName = "destinationType",
                                      propertyValue = "javax.jms.Topic"),
        })
public class MessageListenerBean implements MessageListener {

    private static final Logger
            logger = Logger.getLogger(MessageListenerBean.class.getName());

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

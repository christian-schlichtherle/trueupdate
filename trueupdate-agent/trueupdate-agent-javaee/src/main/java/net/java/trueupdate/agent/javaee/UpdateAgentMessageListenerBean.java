/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.agent.javaee;

import javax.annotation.Resource;
import javax.ejb.*;
import javax.jms.MessageListener;
import net.java.trueupdate.jms.JmsMessageListener;
import net.java.trueupdate.manager.spec.UpdateMessageListener;

/**
 * Filters JMS messages and forwards update messages to the injected
 * {@link UpdateAgentMessageDispatcherBean}.
 *
 * @author Christian Schlichtherle
 */
@MessageDriven(mappedName = "jms/TrueUpdate Agent",
        activationConfig = {
            @ActivationConfigProperty(propertyName = "messageSelector",
                                      propertyValue = "manager = false"),
        })
@DependsOn("UpdateAgentMessageDispatcherBean")
public class UpdateAgentMessageListenerBean
extends JmsMessageListener implements MessageListener {

    @EJB
    private UpdateAgentMessageDispatcherBean updateAgentMessageDispatcher;

    @Resource
    private MessageDrivenContext context;

    @Override protected UpdateMessageListener updateMessageListener() {
        return updateAgentMessageDispatcher;
    }

    @Override protected void onException(Exception ex) {
        context.setRollbackOnly();
    }
}

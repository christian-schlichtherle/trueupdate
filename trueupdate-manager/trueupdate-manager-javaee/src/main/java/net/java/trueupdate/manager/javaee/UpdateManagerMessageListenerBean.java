/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.javaee;

import javax.annotation.Resource;
import javax.ejb.*;
import javax.jms.MessageListener;
import net.java.trueupdate.jms.JmsMessageListener;
import net.java.trueupdate.manager.spec.UpdateMessageListener;

/**
 * Filters JMS messages and forwards update messages to the injected
 * {@link UpdateManagerBean}.
 *
 * @author Christian Schlichtherle
 */
@MessageDriven(mappedName = "jms/TrueUpdate Manager",
        activationConfig = {
            @ActivationConfigProperty(propertyName = "messageSelector",
                                      propertyValue = "manager = true"),
        })
@DependsOn("UpdateManagerBean")
public class UpdateManagerMessageListenerBean
extends JmsMessageListener implements MessageListener {

    @EJB
    private UpdateManagerBean updateManager;

    @Resource
    private MessageDrivenContext context;

    @Override protected UpdateMessageListener updateMessageListener() {
        return updateManager;
    }

    @Override protected void onException(Exception ex) {
        context.setRollbackOnly();
    }
}

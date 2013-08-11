/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.ejb;

import java.io.Serializable;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.*;
import net.java.trueupdate.message.UpdateMessage;
import net.java.trueupdate.message.UpdateMessageListener;

/**
 * @author Christian Schlichtherle
 */
@MessageDriven(messageListenerInterface = MessageListener.class,
        activationConfig = {
            @ActivationConfigProperty(propertyName = "destinationLookup",
                                      propertyValue = "jms/trueupdate/manager"),
            @ActivationConfigProperty(propertyName = "destinationType",
                                      propertyValue = "javax.jms.Queue"),
        })
public class UpdateManager implements MessageListener, UpdateMessageListener {

    private static final Logger
            logger = Logger.getLogger(UpdateManager.class.getName());

    @Override public void onMessage(final Message message) {
        final UpdateMessage updateMessage = updateMessage(message);
        if (null != updateMessage) onUpdateMessage(updateMessage);
    }

    private static @Nullable UpdateMessage updateMessage(final Message message) {
        if (!(message instanceof ObjectMessage)) return null;
        final ObjectMessage objectMessage = (ObjectMessage) message;
        return log(new Callable<UpdateMessage>() {
            @Override public UpdateMessage call() throws Exception {
                final Serializable content = objectMessage.getObject();
                return content instanceof UpdateMessage
                        ? (UpdateMessage) content
                        : null;
            }
        });
    }

    @Override public void onUpdateMessage(final UpdateMessage message) {
        logger.info(message.toString());
    }

    private static @Nullable <V> V log(final Callable<V> task) {
        try {
            return task.call();
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Failure while executing task: ", ex);
            return null;
        }
    }
}

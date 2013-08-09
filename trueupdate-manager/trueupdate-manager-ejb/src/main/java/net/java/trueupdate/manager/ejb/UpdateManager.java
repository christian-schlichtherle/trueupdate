/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.ejb;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.*;

/**
 * @author Christian Schlichtherle
 */
@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationLookup",
                              propertyValue = "jms/trueupdate-manager"),
    @ActivationConfigProperty(propertyName = "destinationType",
                              propertyValue = "javax.jms.Queue"),
})
public class UpdateManager implements MessageListener {

    private static final Logger
            logger = Logger.getLogger(UpdateManager.class.getName());

    @Override public void onMessage(final Message m) {
        try {
            logger.info("--- BEGIN MESSAGE ---");
            logger.log(Level.INFO, "Reply To: {0}", m.getJMSReplyTo());
            if (m instanceof TextMessage)
                logger.log(Level.INFO, "Body: {0}", ((TextMessage) m).getText());
            logger.info("---  END MESSAGE  ---");
        } catch (final JMSException ex) {
            logger.log(Level.WARNING, "Could not process message: ", ex);
        }
    }
}

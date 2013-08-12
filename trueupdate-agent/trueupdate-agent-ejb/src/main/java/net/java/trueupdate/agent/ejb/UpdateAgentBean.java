/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.agent.ejb;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Singleton;
import net.java.trueupdate.message.UpdateMessage;
import net.java.trueupdate.message.UpdateMessageException;
import net.java.trueupdate.message.UpdateMessageListener;

/**
 * @author Christian Schlichtherle
 */
@Singleton
public class UpdateAgentBean extends UpdateMessageListener {

    private static final Logger
            logger = Logger.getLogger(UpdateAgentBean.class.getName());

    @Override
    protected void onSubscriptionSuccessResponse(UpdateMessage message) throws UpdateMessageException {
        logger.log(Level.INFO, "Received subscription success response:\n{0}", message.toString());
    }

    @Override
    protected void onSubscriptionFailureResponse(UpdateMessage message) throws UpdateMessageException {
        logger.log(Level.INFO, "Received subscription failure response:\n{0}", message.toString());
    }

    @Override
    protected void onUpdateAnnouncement(UpdateMessage message) throws UpdateMessageException {
        logger.log(Level.INFO, "Received update announcement:\n{0}", message.toString());
    }

    @Override
    protected void onInstallationSuccessResponse(UpdateMessage message) throws UpdateMessageException {
        logger.log(Level.INFO, "Received installation success response:\n{0}", message.toString());
    }

    @Override
    protected void onInstallationFailureResponse(UpdateMessage message) throws UpdateMessageException {
        logger.log(Level.INFO, "Received installation failure response:\n{0}", message.toString());
    }

    @Override
    protected void onUnsubscriptionSuccessResponse(UpdateMessage message) throws UpdateMessageException {
        logger.log(Level.INFO, "Received unsubscription success response:\n{0}", message.toString());
    }

    @Override
    protected void onUnsubscriptionFailureResponse(UpdateMessage message) throws UpdateMessageException {
        logger.log(Level.INFO, "Received unsubscription failure response:\n{0}", message.toString());
    }
}

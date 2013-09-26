/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.agent.spec;

import java.util.List;
import java.util.logging.Logger;

import net.java.trueupdate.message.*;

/**
 * Listens to events from the update agent.
 *
 * @author Christian Schlichtherle
 */
public class UpdateAgentListener {

    private static final Logger logger = Logger.getLogger(
            UpdateAgentListener.class.getName(),
            LogMessage.class.getName());

    /**
     * Responds to a subscription response.
     * <p>
     * The implementation in the class {@link UpdateAgentListener} just logs
     * any enclosed messages.
     *
     * @see #log(LogMessage)
     */
    public void onSubscriptionResponse(UpdateAgentEvent event)
    throws Exception {
        log(event);
    }

    /**
     * Responds to an update available notice.
     * <p>
     * The implementation in the class {@link UpdateAgentListener} logs any
     * enclosed messages and sends an
     * {@linkplain UpdateAgent#install installation request}.
     *
     * @see #log(LogMessage)
     */
    public void onUpdateNotice(UpdateAgentEvent event)
    throws Exception {
        log(event);
        event.updateAgent().install(event.updateMessage().updateVersion());
    }

    /**
     * Responds to a progress notice.
     * <p>
     * The implementation in the class {@link UpdateAgentListener} just logs
     * any enclosed messages.
     *
     * @see #log(LogMessage)
     */
    public void onProgressNotice(UpdateAgentEvent event)
    throws Exception {
        log(event);
    }

    /**
     * Responds to a redeployment request.
     * <p>
     * The implementation in the class {@link UpdateAgentListener} logs any
     * enclosed messages and sends a
     * {@linkplain UpdateAgent#proceed positive response}.
     *
     * @see #log(LogMessage)
     */
    public void onRedeploymentRequest(UpdateAgentEvent event)
    throws Exception {
        log(event);
        event.updateAgent().proceed();
    }

    /**
     * Responds to an installation success response.
     * <p>
     * The implementation in the class {@link UpdateAgentListener} just logs
     * any enclosed messages.
     *
     * @see #log(LogMessage)
     */
    public void onInstallationSuccessResponse(UpdateAgentEvent event)
    throws Exception {
        log(event);
    }

    /**
     * Responds to an installation failure response.
     * <p>
     * The implementation in the class {@link UpdateAgentListener} just logs
     * any enclosed messages.
     *
     * @see #log(LogMessage)
     */
    public void onInstallationFailureResponse(UpdateAgentEvent event)
    throws Exception {
        log(event);
    }

    private void log(UpdateAgentEvent event) { log(event.updateMessage()); }
    private void log(UpdateMessage message) { log(message.logMessages()); }
    private void log(List<LogMessage> messages) {
        for (LogMessage message : messages) log(message);
    }

    /**
     * Logs the given message.
     * <p>
     * The implementation in the class {@link UpdateAgentListener} uses a
     * {@link Logger} with the name of this class and the resource bundle
     * for the class {@link LogMessage}.
     */
    protected void log(LogMessage message) {
        logger.log(message.level(), message.code(), message.args());
    }
}

/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.agent.spec;

import java.util.List;
import java.util.logging.Logger;
import net.java.trueupdate.message.LogMessage;
import net.java.trueupdate.message.UpdateMessage;

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
     */
    public void onInstallationFailureResponse(UpdateAgentEvent event)
    throws Exception {
        log(event);
    }

    private static void log(UpdateAgentEvent event) {
        log(event.updateMessage());
    }

    private static void log(UpdateMessage message) {
        log(message.logMessages());
    }

    private static void log(List<LogMessage> messages) {
        for (LogMessage lm : messages) lm.log(logger);
    }
}

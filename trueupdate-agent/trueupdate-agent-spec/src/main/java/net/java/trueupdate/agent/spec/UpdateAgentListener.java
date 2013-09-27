/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.agent.spec;

import java.util.List;
import java.util.logging.LogRecord;
import net.java.trueupdate.message.*;

/**
 * Listens to events from the update agent.
 *
 * @author Christian Schlichtherle
 */
public class UpdateAgentListener {

    /**
     * Processes a subscription response.
     * <p>
     * The implementation in the class {@link UpdateAgentListener} just logs
     * any enclosed messages.
     *
     * @see #log(UpdateAgentEvent)
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
     * @see #log(UpdateAgentEvent)
     */
    public void onUpdateNotice(UpdateAgentEvent event)
    throws Exception {
        log(event);
        event.updateAgent().install(event.updateMessage().updateVersion());
    }

    /**
     * Processes a progress notice.
     * <p>
     * The implementation in the class {@link UpdateAgentListener} just logs
     * any enclosed messages.
     *
     * @see #log(UpdateAgentEvent)
     */
    public void onProgressNotice(UpdateAgentEvent event)
    throws Exception {
        log(event);
    }

    /**
     * Responds to a redeployment request.
     * This method must quickly respond to the update manager or otherwise a
     * timeout may occur which would cause the update transaction to roll back!
     * <p>
     * The implementation in the class {@link UpdateAgentListener} logs any
     * enclosed messages and sends a
     * {@linkplain UpdateAgent#proceed positive response}.
     *
     * @see #log(UpdateAgentEvent)
     */
    public void onRedeploymentRequest(UpdateAgentEvent event)
    throws Exception {
        log(event);
        event.updateAgent().proceed();
    }

    /**
     * Processes an installation success response.
     * <p>
     * The implementation in the class {@link UpdateAgentListener} just logs
     * any enclosed messages.
     *
     * @see #log(UpdateAgentEvent)
     */
    public void onInstallationSuccessResponse(UpdateAgentEvent event)
    throws Exception {
        log(event);
    }

    /**
     * Processes an installation failure response.
     * The implementation in the class {@link UpdateAgentListener} just logs
     * any enclosed messages.
     *
     * @see #log(UpdateAgentEvent)
     */
    public void onInstallationFailureResponse(UpdateAgentEvent event)
    throws Exception {
        log(event);
    }

    /**
     * Logs the enclosed log records in the given event.
     * This method forwards the call to {@link #log(LogRecord)} for each
     * enclosed log record.
     * <p>
     * This method must return quickly or otherwise a timeout may occur which
     * would cause the update transaction to roll back!
     */
    protected void log(UpdateAgentEvent event) {
        log(event.updateMessage());
    }

    private void log(UpdateMessage message) { log(message.attachedLogs()); }

    private void log(List<LogRecord> records) {
        for (LogRecord record : records) log(record);
    }

    /**
     * Logs the given record.
     * The implementation in the class {@link UpdateAgentListener} does nothing.
     * Override this method in order to notify the user about the update
     * progress.
     * <p>
     * This method must return quickly or otherwise a timeout may occur which
     * would cause the update transaction to roll back!
     */
    protected void log(LogRecord record) { }
}

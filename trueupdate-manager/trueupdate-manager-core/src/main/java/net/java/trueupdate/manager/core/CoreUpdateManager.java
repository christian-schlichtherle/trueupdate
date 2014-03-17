/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.core;

import edu.umd.cs.findbugs.annotations.CleanupObligation;
import net.java.trueupdate.artifact.spec.ArtifactDescriptor;
import net.java.trueupdate.jaxrs.client.UpdateClient;
import net.java.trueupdate.manager.spec.ActionId;
import net.java.trueupdate.manager.spec.UpdateContext;
import net.java.trueupdate.manager.spec.UpdateInstaller;
import net.java.trueupdate.manager.spec.UpdateManager;
import net.java.trueupdate.manager.spec.cmd.AbstractCommand;
import net.java.trueupdate.manager.spec.cmd.Command;
import net.java.trueupdate.manager.spec.cmd.Commands;
import net.java.trueupdate.manager.spec.cmd.TimeContext;
import net.java.trueupdate.message.UpdateMessage;
import net.java.trueupdate.message.UpdateMessage.Type;
import net.java.trueupdate.message.UpdateMessageListener;
import net.java.trueupdate.util.Services;

import javax.annotation.concurrent.ThreadSafe;
import java.io.File;
import java.net.URI;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static net.java.trueupdate.message.UpdateMessage.Type.*;

/**
 * An abstract update manager.
 *
 * @author Christian Schlichtherle
 */
@ThreadSafe
public abstract class CoreUpdateManager
extends UpdateMessageListener implements UpdateManager {

    private static final Logger logger = Logger.getLogger(
            CoreUpdateManager.class.getName(),
            UpdateMessage.class.getName());

    private static final long HANDSHAKE_TIMEOUT_MILLIS = 10 * 1000;

    private final SessionManager sessionManager = new SessionManager();

    private final Set<UpdateDescriptor> blacklisted =
            Collections.synchronizedSet(new TreeSet<UpdateDescriptor>());

    private final CoreUpdateResolver
            updateResolver = new ConfiguredUpdateResolver();

    private final UpdateInstaller
            updateInstaller = Services.load(UpdateInstaller.class);

    private volatile UpdateClient updateClient;

    protected CoreUpdateManager() {
        logger.log(Level.CONFIG, "manager.installer.class",
                updateInstaller.getClass().getName());
    }

    private UpdateClient updateClient() {
        return null != updateClient
                ? updateClient
                : (updateClient = newUpdateClient());
    }

    private UpdateClient newUpdateClient() {
        return new UpdateClient(updateServiceUri());
    }

    /** Returns the base URI of the update service. */
    protected abstract URI updateServiceUri();

    @Override public void checkForUpdates() throws Exception {

        // Cache subscriptions to allow concurrency.
        final Collection<UpdateMessage>
                sessions = sessionManager.subscriptions();
        if (sessions.isEmpty()) return;
        logger.log(Level.INFO, "manager.check", updateClient().uri());

        // Process the update notices in several steps in order to use minimal
        // locking and account for possible exceptions.
        final class CheckForUpdates implements Callable<Void> {

            final Map<ArtifactDescriptor, UpdateDescriptor>
                    uds = new HashMap<ArtifactDescriptor, UpdateDescriptor>();

            CheckForUpdates() throws Exception {
                downloadUpdateVersionsFromServer();
            }

            void downloadUpdateVersionsFromServer() throws Exception {
                for (final UpdateMessage um : sessions) {
                    final ArtifactDescriptor ad = um.artifactDescriptor();
                    final UpdateDescriptor ud = uds.get(ad);
                    if (null == ud)
                        uds.put(ad, updateDescriptor(ad,
                                updateClient().version(ad)));
                }
            }

            @Override public Void call() throws Exception {
                tellUpdateResolver();
                notifySubscribers();
                return null;
            }

            void tellUpdateResolver() throws Exception {
                synchronized (updateResolver) {
                    updateResolver.restart();
                    for (UpdateMessage um : sessions)
                        for (UpdateDescriptor ud : availableUpdate(um))
                            updateResolver.allocate(ud);
                }
            }

            void notifySubscribers() throws Exception {
                for (UpdateMessage um : sessions)
                    for (UpdateDescriptor ud : availableUpdate(um))
                        if (!blacklisted.contains(ud))
                            sendAndLog(updateNotice(um, ud.updateVersion()));
            }

            @SuppressWarnings("unchecked")
            List<UpdateDescriptor> availableUpdate(UpdateMessage um) {
                final ArtifactDescriptor ad = um.artifactDescriptor();
                final UpdateDescriptor ud = uds.get(ad);
                return ud.updateVersion().equals(ad.version())
                        ? Collections.EMPTY_LIST
                        : Collections.singletonList(ud);
            }

            UpdateMessage updateNotice(UpdateMessage um, String uv) {
                return responseFor(um)
                        .type(UPDATE_NOTICE)
                        .updateVersion(uv)
                        .build();
            }
        } // Reactor

        new CheckForUpdates().call();
    }

    @Override
    protected void onSubscriptionRequest(final UpdateMessage message)
    throws Exception {
        logReceived(message);
        subscribe(message);
        sendAndLog(responseFor(message).type(SUBSCRIPTION_RESPONSE).build());
        checkForUpdates();
    }

    @Override
    protected void onSubscriptionNotice(final UpdateMessage message)
    throws Exception {
        logReceived(message);
        subscribe(message);
        checkForUpdates();
    }

    @Override
    protected void onInstallationRequest(final UpdateMessage message)
    throws Exception {
        logReceived(message);
        subscribe(message);
        UpdateMessage response;
        try {
            install(message);
            response = installationSuccessResponse(message);
        } catch (Exception ex) {
            response = installationFailureResponse(message);
        }
        sendAndLog(response);
    }

    private void install(final UpdateMessage request) throws Exception {

        class Install implements Callable<Void>, UpdateContext, LogChannel {

            ArtifactDescriptor anticipatedDescriptor = request.artifactDescriptor();
            String anticipatedLocation = request.currentLocation();

            File deltaZip;

            private ArtifactDescriptor artifactDescriptor() {
                return request.artifactDescriptor();
            }

            @Override public String currentLocation() {
                return request.currentLocation();
            }

            @Override public String updateLocation() {
                return request.updateLocation();
            }

            @Override public File deltaZip() { return deltaZip; }

            @Override public Void call() throws Exception {
                final UpdateDescriptor ud = updateDescriptor(
                        request.artifactDescriptor(),
                        request.updateVersion());
                net.java.trueupdate.manager.core.LogContext.setChannel(this);
                try {
                    synchronized (updateResolver) {
                        deltaZip = updateResolver.resolve(ud, this);
                    }
                    updateInstaller.install(this);
                    synchronized (updateResolver) {
                        updateResolver.release(ud);
                    }
                } catch (final Exception ex) {
                    logger.log(Level.WARNING, "manager.install.exception", ex);
                    blacklisted.add(ud);
                    logger.log(Level.INFO, "manager.install.blacklist",
                            new Object[] { ud.artifactDescriptor(),
                                           ud.updateVersion() });
                    throw ex;
                } finally {
                    net.java.trueupdate.manager.core.LogContext.resetChannel();
                }
                return null;
            }

            @Override public Command decorate(
                    final Command cmd, final ActionId id) {
                final Command ttx = time(id, cmd);
                return ActionId.UNDEPLOY == id ? undeploy(ttx) : checked(ttx);
            }

            Command time(final ActionId id, final Command cmd) {
                final TimeContext ctx = new TimeContext() {

                    @Override protected Logger logger() { return logger; }

                    @Override
                    protected String startingMessage(TimeContext.Method method) {
                        // Our log message uses its parameters to figure the method.
                        return id.beginKey();
                    }

                    @Override
                    protected String succeededMessage(TimeContext.Method method) {
                        // Our log message uses its parameters to figure the method and status.
                        return id.endKey();
                    }

                    @Override
                    protected String failedMessage(TimeContext.Method method) {
                        // Our log message uses its parameters to figure the method and status.
                        return id.endKey();
                    }
                };
                return Commands.time(ctx, cmd);
            }

            Command undeploy(final Command cmd) {
                return new AbstractCommand() {

                    @Override protected void doStart() throws Exception {
                        onStartUndeployment();
                    }

                    @Override protected void doPerform() throws Exception {
                        cmd.perform();
                        onPerformUndeployment();
                    }

                    @Override protected void doRevert() throws Exception {
                        cmd.revert();
                        onRevertUndeployment();
                    }
                };
            }

            void onStartUndeployment() throws Exception {
                sendRedeploymentRequest();
                final long stop = System.currentTimeMillis()
                        + HANDSHAKE_TIMEOUT_MILLIS;
                synchronized (sessionManager) {
                    while (true) {
                        final UpdateMessage um = sessionManager.get(request);
                        final Type type = um.type();
                        checkCancelled(type);
                        if (PROCEED_REDEPLOYMENT_RESPONSE.equals(type))
                            break;
                        final long remaining = stop - System.currentTimeMillis();
                        if (0 >= remaining)
                            throw new Exception(
                                    "Timeout while waiting for a redeployment response from the update agent.");
                        sessionManager.wait(remaining);
                    }
                }
            }

            void sendRedeploymentRequest() throws Exception {
                final UpdateMessage redeploymentRequest = responseFor(request)
                        .type(REDEPLOYMENT_REQUEST)
                        .artifactDescriptor(artifactDescriptor())
                        .build();
                sendAndLog(redeploymentRequest);
            }

            void checkCancelled(final Type type) throws Exception {
                if (CANCEL_REDEPLOYMENT_RESPONSE.equals(type))
                    throw new Exception(
                            "The update agent has cancelled the update installation.");
            }

            void onPerformUndeployment() {
                anticipatedDescriptor = request
                        .artifactDescriptor()
                        .update()
                        .version(request.updateVersion())
                        .build();
                anticipatedLocation = request.updateLocation();
            }

            void onRevertUndeployment() {
                anticipatedDescriptor = request.artifactDescriptor();
                anticipatedLocation = request.currentLocation();
            }

            Command checked(final Command cmd) {
                return new Command() {

                    @Override public void perform() throws Exception {
                        // Throw an InterruptedException if requested.
                        Thread.sleep(0);
                        // May be undeployed, so check for null.
                        final UpdateMessage um = sessionManager.get(request);
                        if (null != um) checkCancelled(um.type());
                        cmd.perform();
                    }

                    @Override public void revert() throws Exception {
                        cmd.revert();
                    }
                };
            }

            @Override
            public void transmit(final LogRecord record) throws Exception {
                final UpdateMessage um = responseFor(request)
                        .type(PROGRESS_NOTICE)
                        .artifactDescriptor(anticipatedDescriptor)
                        .currentLocation(anticipatedLocation)
                        .build();
                um.attachedLogs().add(record);
                send(um);
            }
        } // Install

        new Install().call();
    }

    @Override
    protected void onProceedRedeploymentResponse(final UpdateMessage message)
    throws Exception {
        logReceived(message);
        subscribe(message);
    }

    @Override
    protected void onCancelRedeploymentResponse(final UpdateMessage message)
    throws Exception {
        logReceived(message);
        subscribe(message);
    }

    @Override
    protected void onUnsubscriptionNotice(final UpdateMessage message)
    throws Exception {
        logReceived(message);
        unsubscribe(message);
    }

    private void sendAndLog(final UpdateMessage message) throws Exception {
        send(message);
        logSent(message);
    }

    private static UpdateMessage installationSuccessResponse(
            final UpdateMessage request) {
        final ArtifactDescriptor ad = request
                .artifactDescriptor()
                .update()
                .version(request.updateVersion())
                .build();
        return responseFor(request)
                .type(INSTALLATION_SUCCESS_RESPONSE)
                .artifactDescriptor(ad)
                .currentLocation(request.updateLocation())
                .updateLocation(null)
                .build();
    }

    private static UpdateMessage installationFailureResponse(
            final UpdateMessage request) {
        return responseFor(request)
                .type(INSTALLATION_FAILURE_RESPONSE)
                .build();
    }

    private static UpdateMessage.Builder<Void> responseFor(
            UpdateMessage message) {
        return message
                .update()
                .timestamp(null)
                .from(message.to())
                .to(message.from())
                .type(null)
                .updateVersion(null);
    }

    /** Sends the given update message. */
    protected abstract void send(UpdateMessage message) throws Exception;

    private static void logReceived(UpdateMessage message) {
        logger.log(Level.FINE, "manager.received", message);
    }

    private static void logSent(UpdateMessage message) {
        logger.log(Level.FINER, "manager.sent", message);
    }

    private void subscribe(UpdateMessage subscription) {
        sessionManager.put(subscription);
    }

    private void unsubscribe(UpdateMessage subscription) {
        sessionManager.remove(subscription);
    }

    @Override public void close() throws Exception {
        synchronized (updateResolver) { updateResolver.close(); }
        sessionManager.close();
    }

    static UpdateDescriptor updateDescriptor(ArtifactDescriptor ad, String uv) {
        return UpdateDescriptor
                .builder()
                .artifactDescriptor(ad)
                .updateVersion(uv)
                .build();
    }

    private class SessionManager {

        final Map<String, UpdateMessage>
                map = new HashMap<String, UpdateMessage>();

        UpdateMessage get(UpdateMessage subscription) {
            return get(subscription.currentLocation());
        }

        synchronized UpdateMessage get(String currentLocation) {
            return map.get(currentLocation);
        }

        synchronized void put(UpdateMessage subscription) {
            map.put(subscription.currentLocation(), subscription);
            notifyAll();
        }

        void remove(UpdateMessage subscription) {
            remove(subscription.currentLocation());
        }

        synchronized void remove(String currentLocation) {
            map.remove(currentLocation);
            notifyAll();
        }

        synchronized Collection<UpdateMessage> subscriptions() {
            return new ArrayList<UpdateMessage>(map.values());
        }

        synchronized void close() throws Exception {
            for (final Iterator<UpdateMessage> it = map.values().iterator();
                    it.hasNext(); ) {
                send(it.next().type(SUBSCRIPTION_NOTICE));
                it.remove();
            }
        }
    } // SessionManager

    @CleanupObligation
    private class ConfiguredUpdateResolver extends CoreUpdateResolver {
        @Override protected UpdateClient updateClient() {
            return CoreUpdateManager.this.updateClient();
        }
    } // ConfiguredUpdateResolver
}

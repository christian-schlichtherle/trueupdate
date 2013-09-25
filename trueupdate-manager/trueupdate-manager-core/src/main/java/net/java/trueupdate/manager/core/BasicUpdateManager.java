/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.core;

import java.io.*;
import java.net.URI;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.logging.*;
import javax.annotation.concurrent.ThreadSafe;
import net.java.trueupdate.artifact.spec.ArtifactDescriptor;
import net.java.trueupdate.jaxrs.client.UpdateClient;
import net.java.trueupdate.manager.spec.*;
import net.java.trueupdate.message.*;
import net.java.trueupdate.message.UpdateMessage.Type;
import static net.java.trueupdate.message.UpdateMessage.Type.*;

/**
 * A basic update manager.
 *
 * @author Christian Schlichtherle
 */
@ThreadSafe
public abstract class BasicUpdateManager
extends UpdateMessageListener implements UpdateManager {

    private static final Logger
            logger = Logger.getLogger(BasicUpdateManager.class.getName());

    private static final long HANDSHAKE_TIMEOUT_MILLIS = 10 * 1000;

    private final StateManager stateManager = new StateManager();

    private final BasicUpdateResolver
            updateResolver = new ConfiguredUpdateResolver();

    private final UpdateInstaller updateInstaller;

    private volatile UpdateClient updateClient;

    protected BasicUpdateManager() { updateInstaller = newUpdateInstaller(); }

    private static UpdateInstaller newUpdateInstaller() {
        final UpdateInstaller ui = ServiceLoader.load(UpdateInstaller.class,
                Thread.currentThread().getContextClassLoader()
                ).iterator().next();
        logger.log(Level.CONFIG,
                "The class name of the update installer is {0} .",
                ui.getClass().getName());
        return ui;
    }

    private UpdateClient updateClient() {
        return null != updateClient
                ? updateClient
                : (updateClient = newUpdateClient());
    }

    private UpdateClient newUpdateClient() {
        return new UpdateClient(updateServiceBaseUri());
    }

    /** Returns the update service base URI. */
    protected abstract URI updateServiceBaseUri();

    @Override public void checkForUpdates() throws Exception {

        // Cache subscriptions to allow concurrency.
        final Collection<UpdateMessage>
                subscriptions = stateManager.subscriptions();
        if (subscriptions.isEmpty()) return;
        logger.log(Level.INFO, "Checking for artifact updates from {0} .",
                updateClient().baseUri());

        // Process the update notices in several steps in order to use minimal
        // locking and account for possible exceptions.
        final class Reactor implements Callable<Void> {

            final Map<ArtifactDescriptor, UpdateDescriptor>
                    uds = new HashMap<ArtifactDescriptor, UpdateDescriptor>();

            Reactor() throws Exception { downloadUpdateVersionsFromServer(); }

            void downloadUpdateVersionsFromServer() throws Exception {
                for (final UpdateMessage um : subscriptions) {
                    final ArtifactDescriptor ad = um.artifactDescriptor();
                    final UpdateDescriptor ud = uds.get(ad);
                    if (null == ud)
                        uds.put(ad, newUpdateDescriptor(ad,
                                updateClient().version(ad)));
                }
            }

            UpdateDescriptor newUpdateDescriptor(ArtifactDescriptor ad,
                                                 String uv) {
                return UpdateDescriptor
                        .builder()
                        .artifactDescriptor(ad)
                        .updateVersion(uv)
                        .build();
            }

            @Override public Void call() throws Exception {
                tellUpdateResolver();
                notifySubscribers();
                return null;
            }

            void tellUpdateResolver() throws Exception {
                synchronized (updateResolver) {
                    updateResolver.restart();
                    for (UpdateMessage um : subscriptions)
                        for (UpdateDescriptor ud : availableUpdate(um))
                            updateResolver.allocate(ud);
                }
            }

            void notifySubscribers() throws Exception {
                for (UpdateMessage um : subscriptions)
                    for (UpdateDescriptor ud : availableUpdate(um))
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

        new Reactor().call();
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
            response = installationFailureResponse(message, ex);
        }
        sendAndLog(response);
    }

    private void install(final UpdateMessage request) throws Exception {

        class UpdateMonitor implements ProgressMonitor {

            Exception ex;

            // TODO: Consider conversation with the update agent about this.
            @Override public boolean isLoggable(Level level) { return true; }

            @Override public void log(
                    final Level level,
                    final String key,
                    final Object... parameters) {
                final LogMessage lm = LogMessage.create(level, key, parameters);
                final UpdateMessage um = responseFor(request)
                        .type(PROGRESS_NOTICE)
                        .logMessages()
                            .add(lm)
                            .inject()
                        .build();
                try {
                    send(um);
                } catch (final Exception ex2) {
                    if (null == ex)
                        logger.log(Level.WARNING, "Cannot send progress notice to update agent:", ex2);
                    ex = ex2;
                }
            }

            @Override public void aboutToRedeploy() throws Exception {
                sendRedeploymentRequest();
                final long stop = System.currentTimeMillis()
                        + HANDSHAKE_TIMEOUT_MILLIS;
                synchronized (stateManager) {
                    while (true) {
                        final UpdateMessage um = stateManager.get(request);
                        final Type type = um.type();
                        if (PROCEED_REDEPLOYMENT_RESPONSE.equals(type))
                            break;
                        if (CANCEL_REDEPLOYMENT_RESPONSE.equals(type))
                            throw new Exception("The update agent has cancelled the redeployment.");
                        final long remaining = stop - System.currentTimeMillis();
                        if (0 > remaining)
                            throw new Exception("Timeout while waiting for redeployment response from update agent.");
                        stateManager.wait(remaining);
                    }
                }
            }

            void sendRedeploymentRequest() throws Exception {
                final UpdateMessage redeploymentRequest = responseFor(request)
                        .type(REDEPLOYMENT_REQUEST)
                        .build();
                sendAndLog(redeploymentRequest);
            }
        } // UpdateMonitor

        final UpdateDescriptor ud = request.updateDescriptor();
        final File diffZip;
        synchronized (updateResolver) {
            diffZip = updateResolver.resolveDiffZip(ud);
        }
        updateInstaller.install(request, diffZip, new UpdateMonitor());
        synchronized (updateResolver) {
            updateResolver.release(ud);
        }
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
                .updateVersion(null)
                .currentLocation(request.updateLocation())
                .updateLocation(null)
                .build();
    }

    private static UpdateMessage installationFailureResponse(
            final UpdateMessage request,
            final Exception ex) {
        final LogMessage lm = LogMessage
                .builder()
                .level(Level.WARNING)
                .message("exception")
                .parameters(ex.toString())
                .build();
        return responseFor(request)
                .type(INSTALLATION_FAILURE_RESPONSE)
                .logMessages()
                    .add(lm)
                    .inject()
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
                .logMessages().inject();
    }

    /** Sends the given update message. */
    protected abstract void send(UpdateMessage message) throws Exception;

    private static void logReceived(UpdateMessage message) {
        logger.log(Level.FINE,
                "Received update message from update agent:\n{0}", message);
    }

    private static void logSent(UpdateMessage message) {
        logger.log(Level.FINER,
                "Sent update message to update agent:\n{0}", message);
    }

    private void subscribe(UpdateMessage subscription) {
        stateManager.put(subscription);
    }

    private void unsubscribe(UpdateMessage subscription) {
        stateManager.remove(subscription);
    }

    @Override public void close() throws Exception {
        synchronized (updateResolver) { updateResolver.close(); }
        stateManager.close();
    }

    private class StateManager {

        final Map<ApplicationDescriptor, UpdateMessage>
                map = new HashMap<ApplicationDescriptor, UpdateMessage>();

        synchronized UpdateMessage get(UpdateMessage subscription) {
            return map.get(subscription.applicationDescriptor());
        }

        synchronized void put(UpdateMessage subscription) {
            map.put(subscription.applicationDescriptor(), subscription);
            notifyAll();
        }

        synchronized void remove(UpdateMessage subscription) {
            map.remove(subscription.applicationDescriptor());
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
    } // SubscriptionManager

    private class ConfiguredUpdateResolver extends BasicUpdateResolver {
        @Override protected UpdateClient updateClient() {
            return BasicUpdateManager.this.updateClient();
        }
    } // ConfiguredUpdateResolver
}

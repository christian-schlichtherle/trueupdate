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
public abstract class CoreUpdateManager
extends UpdateMessageListener implements UpdateManager {

    private static final Logger
            logger = Logger.getLogger(CoreUpdateManager.class.getName());

    private static final long HANDSHAKE_TIMEOUT_MILLIS = 10 * 1000;

    private final StateManager subscriptionManager = new StateManager();

    private final CoreUpdateResolver
            updateResolver = new ConfiguredUpdateResolver();

    private final UpdateInstaller updateInstaller;

    private volatile UpdateClient updateClient;

    protected CoreUpdateManager() { updateInstaller = newUpdateInstaller(); }

    private static UpdateInstaller newUpdateInstaller() {
        final UpdateInstaller ui = ServiceLoader.load(UpdateInstaller.class,
                Thread.currentThread().getContextClassLoader()
                ).iterator().next();
        logger.log(java.util.logging.Level.CONFIG,
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
        return new UpdateClient(updateServiceUri());
    }

    /** Returns the base URI of the update service. */
    protected abstract URI updateServiceUri();

    @Override public void checkForUpdates() throws Exception {

        // Cache subscriptions to allow concurrency.
        final Collection<UpdateMessage>
                subscriptions = subscriptionManager.subscriptions();
        if (subscriptions.isEmpty()) return;
        logger.log(java.util.logging.Level.INFO, "Checking for artifact updates from {0} .",
                updateClient().uri());

        // Process the update notices in several steps in order to use minimal
        // locking and account for possible exceptions.
        final class CheckForUpdates implements Callable<Void> {

            final Map<ArtifactDescriptor, UpdateDescriptor>
                    uds = new HashMap<ArtifactDescriptor, UpdateDescriptor>();

            CheckForUpdates() throws Exception { downloadUpdateVersionsFromServer(); }

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
            response = installationFailureResponse(message, ex);
        }
        sendAndLog(response);
    }

    private void install(final UpdateMessage request) throws Exception {

        class Install implements Callable<Void>, UpdateContext, LogChannel {

            ArtifactDescriptor artifactDescriptor = request.artifactDescriptor();
            String currentLocation = request.currentLocation();
            String updateLocation = request.updateLocation();

            File diffZip;
            Exception ex;

            @Override public String currentLocation() {
                return currentLocation;
            }

            @Override public String updateLocation() {
                return updateLocation;
            }

            @Override public File diffZip() { return diffZip; }

            @Override public Void call() throws Exception {
                LogContext.setChannel(this);
                try {
                    final UpdateDescriptor ud = request.updateDescriptor();
                    synchronized (updateResolver) {
                        diffZip = updateResolver.resolveDiffZip(ud);
                    }
                    updateInstaller.install(Install.this);
                    synchronized (updateResolver) {
                        updateResolver.release(ud);
                    }
                } finally {
                    LogContext.removeChannel();
                }
                return null;
            }

            @Override public void prepareUndeployment() throws Exception {
                sendRedeploymentRequest();
                final long stop = System.currentTimeMillis()
                        + HANDSHAKE_TIMEOUT_MILLIS;
                synchronized (subscriptionManager) {
                    while (true) {
                        final UpdateMessage um = subscriptionManager.get(request);
                        final Type type = um.type();
                        if (PROCEED_REDEPLOYMENT_RESPONSE.equals(type))
                            break;
                        if (CANCEL_REDEPLOYMENT_RESPONSE.equals(type))
                            throw new Exception("The update agent has cancelled the redeployment.");
                        final long remaining = stop - System.currentTimeMillis();
                        if (0 >= remaining)
                            throw new Exception("Timeout while waiting for redeployment response from update agent.");
                        subscriptionManager.wait(remaining);
                    }
                }
            }

            void sendRedeploymentRequest() throws Exception {
                final UpdateMessage redeploymentRequest = responseFor(request)
                        .type(REDEPLOYMENT_REQUEST)
                        .artifactDescriptor(artifactDescriptor)
                        .build();
                sendAndLog(redeploymentRequest);
            }

            @Override public void performUndeployment() throws Exception {
                artifactDescriptor = request.artifactDescriptor()
                        .update()
                        .version(request.updateVersion())
                        .build();
                currentLocation = request.updateLocation();
            }

            @Override public void rollbackUndeployment() throws Exception {
                artifactDescriptor = request.artifactDescriptor();
                currentLocation = request.currentLocation();
            }

            @Override public void commitUndeployment() throws Exception { }

            @Override
            public void transmit(final LogRecord record) throws Exception {
                final UpdateMessage um = responseFor(request)
                        .type(PROGRESS_NOTICE)
                        .artifactDescriptor(artifactDescriptor)
                        .currentLocation(currentLocation)
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
                .updateVersion(null)
                .currentLocation(request.updateLocation())
                .updateLocation(null)
                .build();
    }

    private static UpdateMessage installationFailureResponse(
            final UpdateMessage request,
            final Exception ex) {
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
                .type(null);
    }

    /** Sends the given update message. */
    protected abstract void send(UpdateMessage message) throws Exception;

    private static void logReceived(UpdateMessage message) {
        logger.log(java.util.logging.Level.FINE,
                "Received update message from update agent:\n{0}", message);
    }

    private static void logSent(UpdateMessage message) {
        logger.log(java.util.logging.Level.FINER,
                "Sent update message to update agent:\n{0}", message);
    }

    private void subscribe(UpdateMessage subscription) {
        subscriptionManager.put(subscription);
    }

    private void unsubscribe(UpdateMessage subscription) {
        subscriptionManager.remove(subscription);
    }

    @Override public void close() throws Exception {
        synchronized (updateResolver) { updateResolver.close(); }
        subscriptionManager.close();
    }

    private class StateManager {

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
    } // StateManager

    private class ConfiguredUpdateResolver extends CoreUpdateResolver {
        @Override protected UpdateClient updateClient() {
            return CoreUpdateManager.this.updateClient();
        }
    } // ConfiguredUpdateResolver
}

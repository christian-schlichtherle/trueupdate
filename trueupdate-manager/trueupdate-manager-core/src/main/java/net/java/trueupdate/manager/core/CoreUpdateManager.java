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
import net.java.trueupdate.manager.spec.tx.*;
import net.java.trueupdate.manager.spec.tx.Transactions.LoggerConfig;
import net.java.trueupdate.message.*;
import net.java.trueupdate.message.UpdateMessage.Type;
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

    private static final LoggerConfig loggerConfig = new LoggerConfig() {
        @Override public Logger logger() { return logger; }
    };

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
        logger.log(Level.CONFIG, "manager.installer.class", ui.getClass().getName());
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
                for (final UpdateMessage um : subscriptions) {
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
            response = installationFailureResponse(message);
        }
        sendAndLog(response);
    }

    private void install(final UpdateMessage request) throws Exception {

        class Install implements Callable<Void>, UpdateContext, LogChannel {

            ArtifactDescriptor anticipatedDescriptor = request.artifactDescriptor();
            String anticipatedLocation = request.currentLocation();

            File deltaZip;
            Exception ex;

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
                LogContext.setChannel(this);
                try {
                    final UpdateDescriptor ud = updateDescriptor(
                            request.artifactDescriptor(),
                            request.updateVersion());
                    synchronized (updateResolver) {
                        deltaZip = updateResolver.resolve(ud, this);
                    }
                    updateInstaller.install(this);
                    synchronized (updateResolver) {
                        updateResolver.release(ud);
                    }
                } catch (final Exception ex) {
                    logger.log(Level.WARNING, "manager.install.exception", ex);
                    throw ex;
                } finally {
                    LogContext.removeChannel();
                }
                return null;
            }

            @Override public Transaction decorate(
                    final Action id,
                    final Transaction tx) {
                final Transaction ttx = timed(id, tx);
                return Action.UNDEPLOY == id ? undeploy(ttx) : checked(ttx);
            }

            Transaction timed(Action id, Transaction tx) {
                return Transactions.timed(id.key(), tx, loggerConfig);
            }

            Transaction undeploy(final Transaction tx) {

                class Undeploy extends Transaction {

                    @Override public void prepare() throws Exception {
                        tx.prepare();
                        onPrepareUndeployment();
                    }

                    @Override public void perform() throws Exception {
                        tx.perform();
                        onPerformUndeployment();
                    }

                    @Override public void rollback() throws Exception {
                        tx.rollback();
                        onRollbackUndeployment();
                    }

                    @Override public void commit() throws Exception {
                        tx.commit();
                        onCommitUndeployment();
                    }
                } // Undeploy

                return new Undeploy();
            }

            void onPrepareUndeployment() throws Exception {
                sendRedeploymentRequest();
                final long stop = System.currentTimeMillis()
                        + HANDSHAKE_TIMEOUT_MILLIS;
                synchronized (subscriptionManager) {
                    while (true) {
                        final UpdateMessage um = subscriptionManager.get(request);
                        final Type type = um.type();
                        checkCancelled(type);
                        if (PROCEED_REDEPLOYMENT_RESPONSE.equals(type))
                            break;
                        final long remaining = stop - System.currentTimeMillis();
                        if (0 >= remaining)
                            throw new Exception(
                                    "Timeout while waiting for a redeployment response from the update agent.");
                        subscriptionManager.wait(remaining);
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

            void onPerformUndeployment() throws Exception {
                anticipatedDescriptor = request
                        .artifactDescriptor()
                        .update()
                        .version(request.updateVersion())
                        .build();
                anticipatedLocation = request.updateLocation();
            }

            void onRollbackUndeployment() throws Exception {
                anticipatedDescriptor = request.artifactDescriptor();
                anticipatedLocation = request.currentLocation();
            }

            void onCommitUndeployment() throws Exception { }

            Transaction checked(final Transaction tx) {

                class Checked extends Transaction {

                    @Override public void prepare() throws Exception {
                        tx.prepare();
                    }

                    @Override public void perform() throws Exception {
                        // Throw an InterruptedException if requested.
                        Thread.sleep(0);
                        // May be undeployed, so check for null.
                        final UpdateMessage um = subscriptionManager.get(request);
                        if (null != um) checkCancelled(um.type());
                        tx.perform();
                    }

                    @Override public void rollback() throws Exception {
                        tx.rollback();
                    }

                    @Override public void commit() throws Exception {
                        tx.commit();
                    }
                } // Checked

                return new Checked();
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
                .updateVersion(null)
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
                .type(null);
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
        subscriptionManager.put(subscription);
    }

    private void unsubscribe(UpdateMessage subscription) {
        subscriptionManager.remove(subscription);
    }

    @Override public void close() throws Exception {
        synchronized (updateResolver) { updateResolver.close(); }
        subscriptionManager.close();
    }

    static UpdateDescriptor updateDescriptor(ArtifactDescriptor ad, String uv) {
        return UpdateDescriptor
                .builder()
                .artifactDescriptor(ad)
                .updateVersion(uv)
                .build();
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

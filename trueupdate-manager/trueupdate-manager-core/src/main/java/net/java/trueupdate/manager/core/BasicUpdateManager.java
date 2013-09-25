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

    private final SubscriptionManager
            subscriptionManager = new SubscriptionManager();

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
        final Collection<UpdateMessage> ums = subscriptionManager.get();
        if (ums.isEmpty()) return;
        logger.log(Level.INFO, "Checking for artifact updates from {0} .",
                updateClient().baseUri());

        // Process the update notices in several steps in order to use minimal
        // locking and account for possible exceptions.
        final class Reactor implements Callable<Void> {

            final Map<ArtifactDescriptor, UpdateDescriptor>
                    uds = new HashMap<ArtifactDescriptor, UpdateDescriptor>();

            Reactor() throws Exception { downloadUpdateVersionsFromServer(); }

            void downloadUpdateVersionsFromServer() throws Exception {
                for (final UpdateMessage um : ums) {
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
                    for (UpdateMessage um : ums)
                        for (UpdateDescriptor ud : availableUpdate(um))
                            updateResolver.allocate(ud);
                }
            }

            void notifySubscribers() throws Exception {
                for (UpdateMessage um : ums)
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

    @Override protected void onSubscriptionRequest(final UpdateMessage message)
    throws Exception {
        logReceived(message);
        subscribe(message);
        sendAndLog(responseFor(message).type(SUBSCRIPTION_RESPONSE).build());
        checkForUpdates();
    }

    @Override protected void onSubscriptionNotice(final UpdateMessage message)
    throws Exception {
        logReceived(message);
        subscribe(message);
        checkForUpdates();
    }

    @Override protected void onInstallationRequest(final UpdateMessage message)
    throws Exception {
        logReceived(message);
        UpdateMessage response;
        try {
            install(message);
            response = installationSuccessResponse(message);
        } catch (Exception ex) {
            response = installationFailureResponse(message, ex);
        }
        sendAndLog(response);
    }

    private void install(final UpdateMessage message) throws Exception {
        final UpdateDescriptor descriptor = message.updateDescriptor();
        final File diffZip;
        synchronized (updateResolver) {
            diffZip = updateResolver.resolveDiffZip(descriptor);
        }
        updateInstaller.install(message, diffZip);
        synchronized (updateResolver) {
            updateResolver.release(descriptor);
        }
    }

    private static UpdateMessage installationSuccessResponse(
            UpdateMessage request) {
        final ArtifactDescriptor ad = request
                .artifactDescriptor()
                .update()
                .version(request.updateVersion())
                .build();
        return responseFor(request)
                .type(INSTALLATION_SUCCESS_RESPONSE)
                .artifactDescriptor(ad)
                .updateVersion(null)
                .build();
    }

    private static UpdateMessage installationFailureResponse(
            UpdateMessage request,
            Exception ex) {
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

    @Override protected void onUnsubscriptionNotice(final UpdateMessage message)
    throws Exception {
        logReceived(message);
        unsubscribe(message);
    }

    private void sendAndLog(final UpdateMessage message) throws Exception {
        send(message);
        logSent(message);
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

    @Override public void close() throws Exception {
        synchronized (updateResolver) { updateResolver.close(); }
        subscriptionManager.close();
    }

    private void subscribe(UpdateMessage subscription) {
        subscriptionManager.add(subscription);
    }

    private void unsubscribe(UpdateMessage subscription) {
        subscriptionManager.remove(subscription);
    }

    private class SubscriptionManager {

        final Map<ApplicationDescriptor, UpdateMessage>
                map = new HashMap<ApplicationDescriptor, UpdateMessage>();

        synchronized void add(UpdateMessage subscription) {
            map.put(subscription.applicationDescriptor(), subscription);
        }

        synchronized void remove(UpdateMessage subscription) {
            map.remove(subscription.applicationDescriptor());
        }

        synchronized Collection<UpdateMessage> get() {
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

/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.core;

import java.io.*;
import java.net.URI;
import java.util.*;
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

    private final Map<ApplicationDescriptor, UpdateMessage>
            subscriptions = new HashMap<ApplicationDescriptor, UpdateMessage>();

    private final BasicUpdateResolver
            updateResolver = new ConfiguredUpdateResolver();

    private final UpdateInstaller updateInstaller;
    private volatile UpdateClient updateClient;

    protected BasicUpdateManager() {
        updateInstaller = newUpdateInstaller();
    }

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

    @Override public void checkUpdates() throws Exception {
        if (subscriptions.isEmpty()) return;
        final UpdateClient updateClient = updateClient();
        logger.log(Level.INFO, "Checking for artifact updates from {0} .",
                updateClient.baseUri());
        final Map<ArtifactDescriptor, String>
                updateVersions = new HashMap<ArtifactDescriptor, String>();
        updateResolver.restart();
        for (final UpdateMessage subscription : subscriptions.values()) {
            final ArtifactDescriptor artifactDescriptor =
                    subscription.artifactDescriptor();
            String updateVersion = updateVersions.get(artifactDescriptor);
            if (null == updateVersion)
                updateVersions.put(artifactDescriptor, updateVersion =
                        updateClient.version(artifactDescriptor));
            if (!updateVersion.equals(artifactDescriptor.version())) {
                final UpdateMessage
                        un = updateNotice(subscription, updateVersion);
                updateResolver.allocate(un.updateDescriptor());
                sendAndLog(un);
            }
        }
    }

    private static UpdateMessage updateNotice(UpdateMessage subscription,
                                              String updateVersion) {
        return subscription
                .successResponse()
                .update()
                .type(UPDATE_NOTICE)
                .updateVersion(updateVersion)
                .build();
    }

    @Override protected void onSubscriptionRequest(final UpdateMessage message)
    throws Exception {
        logReceived(message);
        subscribe(message);
        sendAndLog(message.successResponse());
        checkUpdates();
    }

    @Override protected void onSubscriptionNotice(final UpdateMessage message)
    throws Exception {
        logReceived(message);
        subscribe(message);
        checkUpdates();
    }

    private void subscribe(final UpdateMessage message) {
        subscriptions.put(message.applicationDescriptor(), message);
    }

    @Override protected void onInstallationRequest(final UpdateMessage message)
    throws Exception {
        logReceived(message);
        UpdateMessage response;
        try {
            install(message);
            response = installationSuccessResponse(message);
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            response = message.failureResponse(ex);
        }
        sendAndLog(response);
    }

    private void install(final UpdateMessage message) throws Exception {
        final UpdateDescriptor descriptor = message.updateDescriptor();
        final File diffZip = updateResolver.resolveDiffZip(descriptor);
        updateInstaller.install(message, diffZip);
        updateResolver.release(descriptor);
    }

    private static UpdateMessage installationSuccessResponse(
            UpdateMessage message) {
        return message
                .successResponse()
                .update()
                    .artifactDescriptor(updatedArtifactDescriptor(message))
                    .updateVersion(null)
                    .build();
    }

    private static ArtifactDescriptor updatedArtifactDescriptor(
            UpdateMessage message) {
        return message
                .artifactDescriptor()
                .update()
                .version(message.updateVersion())
                .build();
    }

    @Override protected void onUnsubscriptionNotice(final UpdateMessage message)
    throws Exception {
        logReceived(message);
        unsubscribe(message);
    }

    private void unsubscribe(final UpdateMessage message) {
        subscriptions.remove(message.applicationDescriptor());
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
        updateResolver.close();
        persistSubscriptions();
    }

    private void persistSubscriptions() throws Exception {
        for (final Iterator<UpdateMessage> it = subscriptions.values().iterator();
                it.hasNext(); ) {
            send(it.next().type(SUBSCRIPTION_NOTICE));
            it.remove();
        }
    }

    private class ConfiguredUpdateResolver extends BasicUpdateResolver {
        @Override protected UpdateClient updateClient() {
            return BasicUpdateManager.this.updateClient();
        }
    } // ConfiguredUpdateResolver
}

/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.core;

import java.io.*;
import java.util.*;
import java.util.logging.*;
import javax.annotation.concurrent.ThreadSafe;
import net.java.trueupdate.artifact.spec.ArtifactDescriptor;
import net.java.trueupdate.jaxrs.client.UpdateClient;
import net.java.trueupdate.manager.spec.*;
import net.java.trueupdate.manager.spec.UpdateMessage.Type;
import static net.java.trueupdate.manager.spec.UpdateMessage.Type.SUBSCRIPTION_NOTICE;

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

    /** Returns the update client. */
    protected abstract UpdateClient updateClient();

    /** Returns the update installer. */
    protected abstract UpdateInstaller updateInstaller();

    /** Checks for updates and notifies the subscribed agents. */
    @Override
    public void checkUpdates() throws UpdateManagerException {
        if (subscriptions.isEmpty()) return;
        final UpdateClient updateClient = updateClient();
        logger.log(Level.INFO, "Checking for artifact updates from {0} .",
                updateClient.baseUri());
        final Map<ArtifactDescriptor, String>
                updateVersions = new HashMap<ArtifactDescriptor, String>();
        updateResolver.restart();
        try {
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
        } catch (IOException ex) {
            logger.log(Level.WARNING,
                    "Failed to resolve artifact update version:", ex);
        }
    }

    private static UpdateMessage updateNotice(UpdateMessage subscription,
                                              String updateVersion) {
        return subscription
                .successResponse()
                .update()
                .type(Type.UPDATE_NOTICE)
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
        updateInstaller().install(message, diffZip);
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

    @Override protected void onUnsubscriptionRequest(final UpdateMessage message)
    throws Exception {
        onUnsubscriptionNotice(message);
        sendAndLog(message.successResponse());
    }

    @Override protected void onUnsubscriptionNotice(final UpdateMessage message)
    throws Exception {
        logReceived(message);
        unsubscribe(message);
    }

    private void unsubscribe(final UpdateMessage message) {
        subscriptions.remove(message.applicationDescriptor());
    }

    private void sendAndLog(final UpdateMessage message)
    throws UpdateManagerException {
        sendChecked(message);
        logSent(message);
    }

    private void sendChecked(UpdateMessage message)
    throws UpdateManagerException {
        try {
            send(message);
        } catch (UpdateManagerException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new UpdateManagerException(ex);
        }
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

    /**
     * Closes this update manager.
     * This method is idempotent.
     * However, it's the caller's responsibility to make sure that this update
     * manager isn't used anymore after the call to this method, even if it
     * fails.
     */
    @Override
    public void close() throws UpdateManagerException {
        updateResolver.close();
        persistSubscriptions();
    }

    private void persistSubscriptions() throws UpdateManagerException {
        for (final Iterator<UpdateMessage> it = subscriptions.values().iterator();
                it.hasNext(); ) {
            sendChecked(it.next().type(SUBSCRIPTION_NOTICE));
            it.remove();
        }
    }

    private class ConfiguredUpdateResolver extends BasicUpdateResolver {
        @Override protected UpdateClient updateClient() {
            return BasicUpdateManager.this.updateClient();
        }
    } // ConfiguredUpdateResolver
}

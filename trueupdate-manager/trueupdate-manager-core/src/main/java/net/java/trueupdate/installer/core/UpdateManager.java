/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.installer.core;

import java.io.*;
import java.util.*;
import java.util.logging.*;
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
public abstract class UpdateManager extends UpdateMessageListener {

    private static final Logger
            logger = Logger.getLogger(UpdateManager.class.getName());

    private final Map<ApplicationDescriptor, UpdateMessage>
            subscriptions = new HashMap<ApplicationDescriptor, UpdateMessage>();

    private final UpdateResolver
            updateResolver = new ConfiguredUpdateResolver();

    /** Returns the update client. */
    protected abstract UpdateClient updateClient();

    /** Returns the update installer. */
    protected abstract UpdateInstaller updateInstaller();

    /** Checks for updates and notifies the subscribed agents. */
    public void checkUpdates() throws Exception {
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

    @Override protected void onSubscriptionNotice(final UpdateMessage message)
    throws Exception {
        subscribe(logReceived(message));
        checkUpdates();
    }

    @Override protected void onSubscriptionRequest(final UpdateMessage message)
    throws Exception {
        subscribe(logReceived(message));
        sendAndLog(message.successResponse());
        checkUpdates();
    }

    private void subscribe(final UpdateMessage message) {
        subscriptions.put(message.applicationDescriptor(), message);
    }

    @Override protected void onInstallationRequest(UpdateMessage message)
    throws Exception {
        sendAndLog(install(logReceived(message)));
    }

    private UpdateMessage install(final UpdateMessage message) {
        final UpdateDescriptor descriptor = message.updateDescriptor();
        try {
            final File diffZip = updateResolver.resolveDiffZip(descriptor);
            updateInstaller().install(message, diffZip);
            updateResolver.release(descriptor);
            return installationSuccessResponse(message);
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            return message.failureResponse(ex);
        }
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

    @Override protected void onUnsubscriptionNotice(UpdateMessage message)
    throws Exception {
        unsubscribe(logReceived(message));
    }

    @Override protected void onUnsubscriptionRequest(UpdateMessage message)
    throws Exception {
        unsubscribe(logReceived(message));
        sendAndLog(message.successResponse());
    }

    private void unsubscribe(final UpdateMessage message) {
        subscriptions.remove(message.applicationDescriptor());
    }

    private UpdateMessage sendAndLog(UpdateMessage message) throws Exception {
        return logSent(send(message));
    }

    /** Sends the given update message. */
    protected abstract UpdateMessage send(UpdateMessage message)
    throws Exception;

    private static UpdateMessage logReceived(final UpdateMessage message) {
        logger.log(Level.FINE, "Received update message from update agent:\n{0}", message);
        return message;
    }

    private static UpdateMessage logSent(final UpdateMessage message) {
        logger.log(Level.FINER, "Sent update message to update agent:\n{0}", message);
        return message;
    }

    /**
     * Closes this update manager.
     * This method is idempotent.
     * However, it's the caller's responsibility to make sure that this update
     * manager isn't used anymore after the call to this method, even if it
     * fails.
     */
    public void close() throws Exception {
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

    private class ConfiguredUpdateResolver extends UpdateResolver {
        @Override protected UpdateClient updateClient() {
            return UpdateManager.this.updateClient();
        }
    } // ConfiguredUpdateResolver
}

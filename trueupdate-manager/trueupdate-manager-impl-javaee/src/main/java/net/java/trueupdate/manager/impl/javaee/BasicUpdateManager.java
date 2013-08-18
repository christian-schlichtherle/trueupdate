/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.impl.javaee;

import java.util.*;
import java.util.logging.*;
import net.java.trueupdate.artifact.spec.ArtifactDescriptor;
import net.java.trueupdate.jax.rs.client.ArtifactUpdateClient;
import net.java.trueupdate.jax.rs.util.ArtifactUpdateServiceException;
import net.java.trueupdate.manager.spec.*;
import net.java.trueupdate.manager.spec.UpdateMessage.Type;
import static net.java.trueupdate.manager.spec.UpdateMessage.Type.SUBSCRIPTION_NOTICE;

/**
 * A basic update manager.
 * This class has no dependencies on the JMS or Java EE APIs.
 *
 * @author Christian Schlichtherle
 */
public abstract class BasicUpdateManager extends UpdateMessageDispatcher {

    private static final Logger
            logger = Logger.getLogger(BasicUpdateManager.class.getName());

    private final Map<ApplicationDescriptor, UpdateMessage>
            subscriptions = new HashMap<>();

    /** Returns the artifact update client. */
    protected abstract ArtifactUpdateClient updateClient();

    /** Returns the update installer. */
    protected abstract UpdateInstaller updateInstaller();

    /** Sends the given update message. */
    protected abstract UpdateMessage send(final UpdateMessage message)
    throws Exception;

    protected void persistSubscriptions() throws Exception {
        for (UpdateMessage subscription : subscriptions.values())
            send(subscription.type(SUBSCRIPTION_NOTICE));
    }

    protected void checkUpdates() throws Exception {
        if (subscriptions.isEmpty()) return;
        final ArtifactUpdateClient updateClient = updateClient();
        logger.log(Level.INFO, "Checking for artifact updates from: {0}",
                updateClient.baseUri());
        final Map<ArtifactDescriptor, String> versions = new HashMap<>();
        try {
            for (final UpdateMessage subscription : subscriptions.values()) {
                final ArtifactDescriptor artifactDescriptor =
                        subscription.artifactDescriptor();
                String version = versions.get(artifactDescriptor);
                if (null == version)
                    versions.put(artifactDescriptor, version =
                            updateClient.version(artifactDescriptor));
                if (!version.equals(artifactDescriptor.version()))
                    logOutput(send(updateNotice(subscription, version)));
            }
        } catch (ArtifactUpdateServiceException ex) {
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

    @Override protected void onSubscriptionNotice(UpdateMessage message)
    throws Exception {
        logOutput(subscribe(logInput(message)));
        checkUpdates();
    }

    @Override protected void onSubscriptionRequest(UpdateMessage message)
    throws Exception {
        logOutput(send(subscribe(logInput(message))));
        checkUpdates();
    }

    private UpdateMessage subscribe(final UpdateMessage message) {
        subscriptions.put(message.applicationDescriptor(), message);
        return message.successResponse();
    }

    @Override protected void onInstallationRequest(UpdateMessage message)
    throws Exception {
        logOutput(send(install(logInput(message))));
    }

    private UpdateMessage install(UpdateMessage message) {
        try {
            updateInstaller().install(message);
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
        logOutput(unsubscribe(logInput(message)));
    }

    @Override protected void onUnsubscriptionRequest(UpdateMessage message)
    throws Exception {
        logOutput(send(unsubscribe(logInput(message))));
    }

    private UpdateMessage unsubscribe(final UpdateMessage message) {
        subscriptions.remove(message.applicationDescriptor());
        return message.successResponse();
    }

    private static UpdateMessage logInput(final UpdateMessage message) {
        logger.log(Level.FINE, "Input update message:\n{0}", message);
        return message;
    }

    private static UpdateMessage logOutput(final UpdateMessage message) {
        logger.log(Level.FINER, "Output update message:\n{0}", message);
        return message;
    }
}

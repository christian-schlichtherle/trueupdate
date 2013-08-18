/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.impl.javaee;

import java.lang.reflect.UndeclaredThrowableException;
import java.net.*;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.logging.*;
import javax.annotation.*;
import javax.ejb.*;
import javax.jms.*;
import net.java.trueupdate.artifact.spec.ArtifactDescriptor;
import net.java.trueupdate.core.util.SystemProperties;
import net.java.trueupdate.jax.rs.client.ArtifactUpdateClient;
import net.java.trueupdate.jax.rs.util.ArtifactUpdateServiceException;
import net.java.trueupdate.manager.spec.*;
import net.java.trueupdate.manager.spec.UpdateMessage.Type;
import static net.java.trueupdate.manager.spec.UpdateMessage.Type.*;

/**
 * @author Christian Schlichtherle
 */
@Singleton
public class UpdateManagerBean
extends UpdateMessageDispatcher implements UpdateMessageListener {

    private static final Logger
            logger = Logger.getLogger(UpdateManagerBean.class.getName());

    @Resource(name = "serverBaseUri")
    private String serverBaseString;

    @Resource
    private ConnectionFactory connectionFactory;

    private Connection connection;

    @EJB
    private UpdateInstaller installer;

    @Resource(name = "TrueUpdate")
    private Topic destination;

    @Resource(name = "checkUpdatesIntervalMinutes")
    private int checkUpdatesIntervalMinutes;

    @Resource
    private TimerService timerService;

    private final Map<ApplicationDescriptor, UpdateMessage>
            subscriptions = new HashMap<>();

    @PostConstruct private void postConstruct() {
        wrap(new Callable<Void>() {
            @Override public Void call() throws Exception {
                initConnection();
                initTimer();
                return null;
            }
        });
    }

    private void initConnection() throws JMSException {
        connection = connectionFactory.createConnection();
    }

    private void initTimer() {
        logger.log(Level.CONFIG, "The configured update interval is {0} minutes.",
                checkUpdatesIntervalMinutes);
        final long intervalMillis = checkUpdatesIntervalMinutes * 60L * 1000;
        timerService.createTimer(intervalMillis, intervalMillis, null);
    }

    @PreDestroy private void preDestroy() {
        wrap(new Callable<Void>() {
            @Override public Void call() throws Exception {
                persistSubscriptions();
                closeConnection();
                return null;
            }
        });
    }

    private void persistSubscriptions() throws Exception {
        for (UpdateMessage subscription : subscriptions.values())
            send(subscription.type(SUBSCRIPTION_NOTICE));
    }

    private void closeConnection() throws JMSException {
        connection.close();
    }

    @Timeout
    public void checkUpdates() throws URISyntaxException, JMSException {
        if (subscriptions.isEmpty()) return;
        final URI uri = serverBaseUri();
        logger.log(Level.INFO, "Checking for artifact updates from: {0}", uri);
        final ArtifactUpdateClient client = new ArtifactUpdateClient(uri);
        final Map<ArtifactDescriptor, String> versions = new HashMap<>();
        try {
            for (final UpdateMessage subscription : subscriptions.values()) {
                final ArtifactDescriptor artifactDescriptor =
                        subscription.artifactDescriptor();
                String version = versions.get(artifactDescriptor);
                if (null == version)
                    versions.put(artifactDescriptor,
                            version = client.version(artifactDescriptor));
                if (!version.equals(artifactDescriptor.version()))
                    logOutput(send(updateNotice(subscription, version)));
            }
        } catch (ArtifactUpdateServiceException ex) {
            logger.log(Level.WARNING,
                    "Failed to resolve artifact update version:", ex);
        }
    }

    private URI serverBaseUri() throws URISyntaxException {
        return new URI(SystemProperties.resolve(serverBaseString));
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
            installer.install(message);
            return message
                    .successResponse()
                    .update()
                        .artifactDescriptor(updatedArtifactDescriptor(message))
                        .updateVersion(null)
                        .build();
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            return message.failureResponse(ex);
        }
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

    private UpdateMessage send(final UpdateMessage message)
    throws JMSException {
        final Session s = connection.createSession(true, 0);
        try {
            final Message m = s.createObjectMessage(message);
            m.setBooleanProperty("manager", message.type().forManager());
            s.createProducer(destination).send(m);
            return message;
        } finally {
            s.close();
        }
    }

    private static @Nullable <V> V wrap(final Callable<V> task) {
        try {
            return task.call();
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new UndeclaredThrowableException(ex);
        }
    }
}

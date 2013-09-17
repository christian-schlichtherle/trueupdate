/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.sample.javaee.client;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.logging.*;
import javax.annotation.*;
import javax.ejb.*;
import net.java.trueupdate.agent.mini.UpdateAgentContext;
import net.java.trueupdate.agent.spec.*;
import net.java.trueupdate.manager.spec.UpdateMessage;

/**
 * @author Christian Schlichtherle
 */
@Startup
@Singleton
@DependsOn("UpdateAgentBuilderBean")
public class UpdateClientBean extends ApplicationListener {

    private static final Logger
            logger = Logger.getLogger(UpdateClientBean.class.getName());

    private static ResourceBundle
            bundle = ResourceBundle.getBundle(UpdateClientBean.class.getName());

    private final UpdateAgent updateAgent =
            new UpdateAgentContext()
                .applicationParameters()
                    .applicationListener(this)
                    .applicationDescriptor()
                        .artifactDescriptor()
                            .groupId(parameter("groupId"))
                            .artifactId(parameter("artifactId"))
                            .version(parameter("version"))
                            .classifier(parameter("classifier"))
                            .extension(parameter("extension"))
                            .inject()
                        .currentLocation(parameter("currentLocation"))
                        .inject()
                    .updateLocation(parameter("updateLocation"))
                    .inject()
                .messagingParameters()
                    .connectionFactory(parameter("connectionFactory"))
                    .from(parameter("agent"))
                    .to(parameter("manager"))
                    .inject()
                .build();

    private boolean subscribed;

    private static @Nullable String parameter(String key) {
        try { return bundle.getString(key); }
        catch (MissingResourceException ex) { return null; }
    }

    @PostConstruct private void subscribe() {
        if (subscribed) return;
        wrap(new Callable<Void>() {
            @Override public Void call() throws Exception {
                updateAgent.subscribe();
                return null;
            }
        });
        subscribed = true;
    }

    @PreDestroy private void unsubscribe() {
        if (!subscribed) return;
        wrap(new Callable<Void>() {
            @Override public Void call() throws Exception {
                updateAgent.unsubscribe();
                return null;
            }
        });
        subscribed = false;
    }

    private @Nullable <V> V wrap(final Callable<V> task) {
        try {
            return task.call();
        } catch (RuntimeException ex) {
            throw ex;
        } catch (final Exception ex) {
            throw new EJBException(ex);
        }
    }

    @Override public void onSubscriptionSuccessResponse(UpdateMessage message)
    throws Exception {
        logReceived(message);
    }

    @Override public void onSubscriptionFailureResponse(UpdateMessage message)
    throws Exception {
        logReceived(message);
    }

    @Override public void onUpdateNotice(final UpdateMessage message)
    throws Exception {
        logReceived(message);
        updateAgent.install(message.updateVersion());
    }

    @Override public void onInstallationSuccessResponse(UpdateMessage message)
    throws Exception {
        logReceived(message);
    }

    @Override public void onInstallationFailureResponse(UpdateMessage message)
    throws Exception {
        logReceived(message);
    }

    @Override public void onUnsubscriptionSuccessResponse(UpdateMessage message)
    throws Exception {
        logReceived(message);
    }

    @Override public void onUnsubscriptionFailureResponse(UpdateMessage message)
    throws Exception {
        logReceived(message);
    }

    private static void logReceived(UpdateMessage message) {
        logger.log(Level.FINE,
                "Received update message from update manager:\n{0}", message);
    }
}

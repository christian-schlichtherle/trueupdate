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

    @EJB
    private UpdateAgent.Builder<?, ?> updateAgentBuilder;

    @Resource
    private SessionContext context;

    @PostConstruct private void subscribe() {
        log(new Callable<Void>() {
            @Override public Void call() throws Exception {
                updateAgent().subscribe();
                return null;
            }
        });
    }

    @PreDestroy private void unsubscribe() {
        log(new Callable<Void>() {
            @Override public Void call() throws Exception {
                updateAgent().unsubscribe();
                return null;
            }
        });
    }

    private @Nullable <V> V log(final Callable<V> task) {
        try {
            return task.call();
        } catch (RuntimeException ex) {
            throw ex;
        } catch (final Exception ex) {
            context.setRollbackOnly();
            logger.log(Level.WARNING, "Error while processing task.", ex);
            return null;
        }
    }

    private UpdateAgent updateAgent() {
        // The result may get cached, too.
        return updateAgentBuilder
                .applicationParameters()
                    .applicationListener(this)
                    .applicationDescriptor()
                        .artifactDescriptor()
                            .groupId(lookup("groupId"))
                            .artifactId(lookup("artifactId"))
                            .version(lookup("version"))
                            .classifier(lookup("classifier"))
                            .extension(lookup("extension"))
                            .inject()
                        .currentLocation(lookup("currentLocation"))
                        .inject()
                    .updateLocation(lookup("updateLocation"))
                    .inject()
                .build();
    }

    private static @Nullable String lookup(String key) {
        try { return bundle.getString(key); }
        catch (MissingResourceException ex) { return null; }
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
        updateAgent().install(message.updateVersion());
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

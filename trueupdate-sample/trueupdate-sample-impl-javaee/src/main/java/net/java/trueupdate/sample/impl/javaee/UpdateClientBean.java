/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.sample.impl.javaee;

import java.net.URI;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.concurrent.Callable;
import java.util.logging.*;
import javax.annotation.*;
import javax.ejb.*;
import net.java.trueupdate.agent.spec.*;
import net.java.trueupdate.manager.spec.*;

/**
 * @author Christian Schlichtherle
 */
@Startup
@Singleton
public class UpdateClientBean extends ApplicationListener {

    private static final Logger
            logger = Logger.getLogger(UpdateClientBean.class.getName());

    private static ResourceBundle
            bundle = ResourceBundle.getBundle(UpdateClientBean.class.getName());

    @EJB
    private UpdateAgent.Builder updateAgentBuilder;

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
            logger.log(Level.SEVERE, "Error while processing task.", ex);
            return null;
        }
    }

    private UpdateAgent updateAgent() {
        // The update agent should normally get cached, but isn't done here for
        // testing purposes.
        return updateAgentBuilder
                .applicationParameters()
                    .applicationListener(this)
                    .applicationDescriptor()
                        .artifactDescriptor()
                            .groupId(lookupString("groupId"))
                            .artifactId(lookupString("artifactId"))
                            .version(lookupString("version"))
                            .classifier(lookupString("classifier"))
                            .extension(lookupString("extension"))
                            .inject()
                        .currentLocation(lookupUri("currentLocation"))
                        .inject()
                    .updateLocation(lookupUri("updateLocation"))
                    .inject()
                .build();
    }

    private URI lookupUri(String key) { return URI.create(lookupString(key)); }

    private @Nullable String lookupString(String key) {
        try { return bundle.getString(key); }
        catch (MissingResourceException ex) { return null; }
    }

    @Override public void onSubscriptionSuccessResponse(UpdateMessage message)
    throws Exception {
        log(message);
    }

    @Override public void onSubscriptionFailureResponse(UpdateMessage message)
    throws Exception {
        log(message);
    }

    @Override public void onUpdateAnnouncement(UpdateMessage message)
    throws Exception {
        updateAgent().install(log(message).updateVersion());
    }

    @Override public void onInstallationSuccessResponse(UpdateMessage message)
    throws Exception {
        log(message);
    }

    @Override public void onInstallationFailureResponse(UpdateMessage message)
    throws Exception {
        log(message);
    }

    @Override public void onUnsubscriptionSuccessResponse(UpdateMessage message)
    throws Exception {
        log(message);
    }

    @Override public void onUnsubscriptionFailureResponse(UpdateMessage message)
    throws Exception {
        log(message);
    }

    private UpdateMessage log(final UpdateMessage message) {
        logger.log(Level.INFO, "Received update message:\n{0}", message);
        return message;
    }
}
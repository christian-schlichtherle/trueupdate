/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.agent.ejb;

import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.SessionContext;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import net.java.trueupdate.agent.spec.UpdateAgent;
import net.java.trueupdate.agent.spec.UpdateAgentFactory;
import net.java.trueupdate.agent.spec.UpdateManagerListener;
import net.java.trueupdate.artifact.spec.ArtifactDescriptor;
import net.java.trueupdate.message.UpdateMessage;
import net.java.trueupdate.message.UpdateMessageException;

/**
 * @author Christian Schlichtherle
 */
@Startup
@Singleton
public class TestBean extends UpdateManagerListener {

    @EJB
    private UpdateAgentFactory updateAgentFactory;

    private static final Logger
            logger = Logger.getLogger(TestBean.class.getName());

    private static final ArtifactDescriptor ARTIFACT_DESCRIPTOR =
            ArtifactDescriptor
                .create()
                .groupId("net.java.truevfs")
                .artifactId("truevfs-kernel-spec")
                .version("0.9")
                .build();

    @Resource
    private SessionContext context;

    @PostConstruct
    public void subscribe() {
        log(new Callable<Void>() {
            @Override public Void call() throws Exception {
                updateAgent().subscribe();
                return null;
            }
        });
    }

    @PreDestroy
    public void unsubscribe() {
        log(new Callable<Void>() {
            @Override public Void call() throws Exception {
                updateAgent().unsubscribe();
                return null;
            }
        });
    }

    private UpdateAgent updateAgent() {
        return updateAgentFactory.newUpdateAgent(ARTIFACT_DESCRIPTOR, this);
    }

    private @Nullable <V> V log(final Callable<V> task) {
        try {
            return task.call();
        } catch (final Exception ex) {
            context.setRollbackOnly();
            logger.log(Level.SEVERE, "Could not send message.", ex);
            return null;
        }
    }

    @Override
    public void onSubscriptionSuccessResponse(UpdateMessage message) throws UpdateMessageException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void onSubscriptionFailureResponse(UpdateMessage message) throws UpdateMessageException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void onInstallationSuccessResponse(UpdateMessage message) throws UpdateMessageException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void onInstallationFailureResponse(UpdateMessage message) throws UpdateMessageException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void onUnsubscriptionSuccessResponse(UpdateMessage message) throws UpdateMessageException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void onUnsubscriptionFailureResponse(UpdateMessage message) throws UpdateMessageException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}

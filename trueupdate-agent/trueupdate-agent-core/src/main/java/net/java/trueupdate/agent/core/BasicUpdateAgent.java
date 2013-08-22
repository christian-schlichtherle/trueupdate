/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.agent.core;

import java.net.URI;
import javax.annotation.Nullable;
import net.java.trueupdate.agent.spec.ApplicationParameters;
import net.java.trueupdate.agent.spec.UpdateAgent;
import net.java.trueupdate.agent.spec.UpdateAgentException;
import net.java.trueupdate.manager.spec.ApplicationDescriptor;
import net.java.trueupdate.manager.spec.UpdateMessage;
import static net.java.trueupdate.manager.spec.UpdateMessage.Type.*;

/**
 * A basic update agent.
 *
 * @author Christian Schlichtherle
 */
public abstract class BasicUpdateAgent implements UpdateAgent {

    private static final URI
            AGENT_URI = URI.create("agent"),
            MANAGER_URI = URI.create("manager");

    protected abstract UpdateMessageDispatcher updateMessageDispatcher();

    protected abstract ApplicationParameters applicationParameters();

    protected URI from() { return AGENT_URI; }

    protected URI to() { return MANAGER_URI; }

    @Override public void subscribe() throws UpdateAgentException {
        updateMessageDispatcher().subscribe(applicationParameters());
        send(SUBSCRIPTION_REQUEST, null);
    }

    @Override public void unsubscribe() throws UpdateAgentException {
        send(UNSUBSCRIPTION_NOTICE, null);
        updateMessageDispatcher().unsubscribe(applicationParameters());
    }

    @Override public void install(String version) throws UpdateAgentException {
        send(INSTALLATION_REQUEST, version);
    }

    private UpdateMessage send(final UpdateMessage.Type type,
                               final @Nullable String updateVersion)
    throws UpdateAgentException {
        final ApplicationParameters ap = applicationParameters();
        final ApplicationDescriptor ad = ap.applicationDescriptor();
        final UpdateMessage message = UpdateMessage
                    .builder()
                    .from(from())
                    .to(to())
                    .type(type)
                    .artifactDescriptor(ad.artifactDescriptor())
                    .currentLocation(ad.currentLocation())
                    .updateLocation(ap.updateLocation())
                    .updateVersion(updateVersion)
                    .build();
        try { return send(message); }
        catch (RuntimeException ex) { throw ex; }
        catch (Exception ex) { throw new UpdateAgentException(ex); }
    }

    protected abstract UpdateMessage send(UpdateMessage message)
    throws Exception;
}

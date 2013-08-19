/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.agent.impl.javaee;

import java.net.URI;
import javax.annotation.Nullable;
import net.java.trueupdate.agent.spec.*;
import net.java.trueupdate.artifact.spec.ArtifactDescriptor;
import net.java.trueupdate.manager.spec.*;
import static net.java.trueupdate.manager.spec.UpdateMessage.Type.*;

/**
 * A basic update agent.
 * This class has no dependencies on the JMS or Java EE API.
 *
 * @author Christian Schlichtherle
 */
abstract class BasicUpdateAgent implements UpdateAgent {

    private static final URI
            AGENT_URI = URI.create("agent"),
            MANAGER_URI = URI.create("manager");

    private ArtifactDescriptor artifactDescriptor() {
        return applicationDescriptor().artifactDescriptor();
    }

    private URI currentLocation() {
        return applicationDescriptor().currentLocation();
    }

    private ApplicationDescriptor applicationDescriptor() {
        return applicationParameters().applicationDescriptor();
    }

    private URI updateLocation() {
        return applicationParameters().updateLocation();
    }

    protected abstract ApplicationParameters applicationParameters();

    @Override public void subscribe() throws UpdateAgentException {
        send(SUBSCRIPTION_REQUEST, null);
    }

    @Override public void install(String version) throws UpdateAgentException {
        send(INSTALLATION_REQUEST, version);
    }

    @Override public void unsubscribe() throws UpdateAgentException {
        send(UNSUBSCRIPTION_NOTICE, null);
    }

    protected UpdateMessage send(final UpdateMessage.Type type,
                                 final @Nullable String updateVersion)
    throws UpdateAgentException {
        final UpdateMessage message = UpdateMessage
                    .builder()
                    .from(AGENT_URI)
                    .to(MANAGER_URI)
                    .type(type)
                    .artifactDescriptor(artifactDescriptor())
                    .currentLocation(currentLocation())
                    .updateLocation(updateLocation())
                    .updateVersion(updateVersion)
                    .build();
        try { return send(message); }
        catch (RuntimeException ex) { throw ex; }
        catch (Exception ex) { throw new UpdateAgentException(ex); }
    }

    protected abstract UpdateMessage send(UpdateMessage message)
    throws Exception;
}

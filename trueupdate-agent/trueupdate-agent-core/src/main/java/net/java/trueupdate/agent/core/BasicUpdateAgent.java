/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.agent.core;

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

    protected abstract ApplicationParameters applicationParameters();

    protected abstract String from();

    protected abstract String to();

    @Override public void subscribe() throws UpdateAgentException {
        send(SUBSCRIPTION_REQUEST, null);
    }

    @Override public void unsubscribe() throws UpdateAgentException {
        send(UNSUBSCRIPTION_NOTICE, null);
    }

    @Override public void install(String version) throws UpdateAgentException {
        send(INSTALLATION_REQUEST, version);
    }

    private void send(final UpdateMessage.Type type,
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
        try { send(message); }
        catch (RuntimeException ex) { throw ex; }
        catch (Exception ex) { throw new UpdateAgentException(ex); }
    }

    protected abstract void send(UpdateMessage message)
    throws Exception;
}

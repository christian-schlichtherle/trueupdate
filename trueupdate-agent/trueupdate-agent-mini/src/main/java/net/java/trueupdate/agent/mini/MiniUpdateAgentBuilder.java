/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.agent.mini;

import javax.annotation.*;
import net.java.trueupdate.agent.core.BasicUpdateAgentBuilder;
import net.java.trueupdate.agent.spec.UpdateAgent;

/**
 *
 * @author Christian Schlichtherle
 */
public class MiniUpdateAgentBuilder
extends BasicUpdateAgentBuilder<MiniUpdateAgentBuilder, Void> {

    @CheckForNull
    private TransportParameters transportParameters;

    public TransportParameters.Builder<MiniUpdateAgentBuilder> transportParameters() {
        return new TransportParameters.Builder<MiniUpdateAgentBuilder>() {
            @Override public MiniUpdateAgentBuilder inject() {
                return transportParameters(build());
            }
        };
    }

    public MiniUpdateAgentBuilder transportParameters(
            final @Nullable TransportParameters transportParameters) {
        this.transportParameters = transportParameters;
        return this;
    }

    @Override public UpdateAgent build() {
        return new MiniUpdateAgent(applicationParameters, transportParameters);
    }
}

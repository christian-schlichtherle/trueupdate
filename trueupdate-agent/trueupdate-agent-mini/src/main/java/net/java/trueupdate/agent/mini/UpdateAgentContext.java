/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.agent.mini;

import javax.annotation.*;
import net.java.trueupdate.agent.core.BasicUpdateAgentBuilder;
import net.java.trueupdate.agent.spec.UpdateAgent;

/**
 * A context for building an update agent.
 *
 * @author Christian Schlichtherle
 */
public class UpdateAgentContext
extends BasicUpdateAgentBuilder<UpdateAgentContext, Void> {

    @CheckForNull
    private MessagingParameters messagingParameters;

    public MessagingParameters.Builder<UpdateAgentContext> messagingParameters() {
        return new MessagingParameters.Builder<UpdateAgentContext>() {
            @Override public UpdateAgentContext inject() {
                return messagingParameters(build());
            }
        };
    }

    public UpdateAgentContext messagingParameters(
            final @Nullable MessagingParameters messagingParameters) {
        this.messagingParameters = messagingParameters;
        return this;
    }

    @Override public UpdateAgent build() {
        return new ConfiguredUpdateAgent(applicationParameters,
                                         messagingParameters);
    }
}

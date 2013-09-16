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
    private MessagingParameters messagingParameters;

    public MessagingParameters.Builder<MiniUpdateAgentBuilder> messagingParameters() {
        return new MessagingParameters.Builder<MiniUpdateAgentBuilder>() {
            @Override public MiniUpdateAgentBuilder inject() {
                return messagingParameters(build());
            }
        };
    }

    public MiniUpdateAgentBuilder messagingParameters(
            final @Nullable MessagingParameters messagingParameters) {
        this.messagingParameters = messagingParameters;
        return this;
    }

    @Override public UpdateAgent build() {
        return new MiniUpdateAgent(applicationParameters, messagingParameters);
    }
}

/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.agent.impl.core;

import javax.annotation.CheckForNull;
import net.java.trueupdate.agent.spec.*;
import net.java.trueupdate.agent.spec.UpdateAgent.Builder;

/**
 * A basic update agent builder.
 *
 * @author Christian Schlichtherle
 */
@SuppressWarnings("ProtectedField")
public abstract class BasicUpdateAgentBuilder implements UpdateAgent.Builder {

    @CheckForNull
    protected ApplicationParameters applicationParameters;

    @Override
    public ApplicationParameters.Builder<Builder> applicationParameters() {
        return new ApplicationParameters.Builder<Builder>() {
            @Override
            public Builder inject() { return applicationParameters(build()); }
        };
    }

    @Override public Builder applicationParameters(
            final ApplicationParameters applicationParameters) {
        this.applicationParameters = applicationParameters;
        return this;
    }
}

/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.agent.core;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import net.java.trueupdate.agent.spec.*;

/**
 * A basic update agent builder.
 *
 * @param <B> The type of this builder.
 * @param <P> The type of the parent builder, if defined.
 * @author Christian Schlichtherle
 */
@SuppressWarnings({ "ProtectedField", "unchecked", "rawtypes" })
public abstract class BasicUpdateAgentBuilder<
        B extends BasicUpdateAgentBuilder<B, P>,
        P>
implements UpdateAgent.Builder<B, P> {

    @CheckForNull
    protected ApplicationParameters applicationParameters;

    @Override
    public ApplicationParameters.Builder<B> applicationParameters() {
        return new ApplicationParameters.Builder<B>() {
            @Override public B inject() {
                return applicationParameters(build());
            }
        };
    }

    @Override public B applicationParameters(
            final @Nullable ApplicationParameters applicationParameters) {
        this.applicationParameters = applicationParameters;
        return (B) this;
    }

    @Override public P inject() {
        throw new IllegalStateException("No parent builder defined.");
    }
}

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
 * @param <T> The type of this basic update agent builder.
 * @author Christian Schlichtherle
 */
@SuppressWarnings("ProtectedField")
public abstract class BasicUpdateAgentBuilder<
        T extends BasicUpdateAgentBuilder<T>>
implements UpdateAgent.Builder<T> {

    @CheckForNull
    protected ApplicationParameters applicationParameters;

    @Override
    public ApplicationParameters.Builder<T> applicationParameters() {
        return new ApplicationParameters.Builder<T>() {
            @Override
            public T inject() { return applicationParameters(build()); }
        };
    }

    @Override
    @SuppressWarnings("unchecked")
    public T applicationParameters(
            final @Nullable ApplicationParameters applicationParameters) {
        this.applicationParameters = applicationParameters;
        return (T) this;
    }
}

/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.agent.servlets;

import javax.annotation.CheckForNull;
import net.java.trueupdate.agent.servlets.ci.UpdateAgentCi;
import net.java.trueupdate.agent.spec.ApplicationParameters;
import net.java.trueupdate.jms.MessagingParameters;
import static net.java.trueupdate.util.Objects.requireNonNull;

/**
 * Update agent parameters.
 *
 * @author Christian Schlichtherle
 */
final class UpdateAgentParameters {

    private final ApplicationParameters applicationParameters;
    private final MessagingParameters messagingParameters;

    UpdateAgentParameters(final Builder b) {
        this.applicationParameters = requireNonNull(b.applicationParameters);
        this.messagingParameters = requireNonNull(b.messagingParameters);
    }

    /** Returns a new builder for update agent parameters. */
    static Builder builder() { return new Builder(); }

    ApplicationParameters applicationParameters() {
        return applicationParameters;
    }

    MessagingParameters messagingParameters() {
        return messagingParameters;
    }

    /** A builder for update agent parameters. */
    @SuppressWarnings({ "PackageVisibleField", "PackageVisibleInnerClass" })
    static class Builder {

        @CheckForNull ApplicationParameters applicationParameters;
        @CheckForNull MessagingParameters messagingParameters;

        /** Parses the given configuration. */
        Builder parse(final UpdateAgentCi ci) {
            applicationParameters = ApplicationParameters
                    .builder()
                    .parse(ci.application)
                    .build();
            messagingParameters = MessagingParameters
                    .builder()
                    .parseNaming(ci.naming)
                    .parseMessaging(ci.messaging)
                    .build();
            return this;
        }

        UpdateAgentParameters build() {
            return new UpdateAgentParameters(this);
        }
    } // Builder
}

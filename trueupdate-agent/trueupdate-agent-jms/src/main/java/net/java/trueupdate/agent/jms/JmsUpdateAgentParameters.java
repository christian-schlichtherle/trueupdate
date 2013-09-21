/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.agent.jms;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import net.java.trueupdate.agent.jms.dto.JmsUpdateAgentParametersDto;
import net.java.trueupdate.agent.spec.ApplicationParameters;
import net.java.trueupdate.jms.MessagingParameters;
import static net.java.trueupdate.util.Objects.requireNonNull;

/**
 * JMS Update Agent Parameters.
 *
 * @author Christian Schlichtherle
 */
public final class JmsUpdateAgentParameters {

    private final ApplicationParameters applicationParameters;
    private final MessagingParameters messagingParameters;

    JmsUpdateAgentParameters(final Builder b) {
        this.applicationParameters = requireNonNull(b.applicationParameters);
        this.messagingParameters = requireNonNull(b.messagingParameters);
    }

    /** Parses the given configuration item. */
    public static JmsUpdateAgentParameters parse(JmsUpdateAgentParametersDto ci) {
        return builder().parse(ci).build();
    }

    /** Returns a new builder for update agent parameters. */
    public static Builder builder() { return new Builder(); }

    public ApplicationParameters applicationParameters() {
        return applicationParameters;
    }

    public MessagingParameters messagingParameters() {
        return messagingParameters;
    }

    /** A builder for update agent parameters. */
    @SuppressWarnings({ "PackageVisibleField", "PackageVisibleInnerClass" })
    static class Builder {

        @CheckForNull ApplicationParameters applicationParameters;
        @CheckForNull MessagingParameters messagingParameters;

        /** Selectively parses the given configuration item. */
        public Builder parse(final JmsUpdateAgentParametersDto ci) {
            if (null != ci.application)
                applicationParameters = ApplicationParameters.parse(ci.application);
            if (null != ci.messaging)
                messagingParameters = MessagingParameters.parse(ci.messaging);
            return this;
        }

        public Builder applicationParameters(
                final @Nullable ApplicationParameters applicationParameters) {
            this.applicationParameters = applicationParameters;
            return this;
        }

        public Builder messagingParameters(
                final @Nullable MessagingParameters messagingParameters) {
            this.messagingParameters = messagingParameters;
            return this;
        }

        public JmsUpdateAgentParameters build() {
            return new JmsUpdateAgentParameters(this);
        }
    } // Builder
}

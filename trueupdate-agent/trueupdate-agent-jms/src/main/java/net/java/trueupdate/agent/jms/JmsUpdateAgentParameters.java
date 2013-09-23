/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.agent.jms;

import java.net.URL;
import java.util.ServiceConfigurationError;
import javax.annotation.*;
import javax.annotation.concurrent.Immutable;
import javax.xml.bind.JAXB;
import net.java.trueupdate.agent.jms.dto.JmsUpdateAgentParametersDto;
import net.java.trueupdate.agent.spec.ApplicationParameters;
import net.java.trueupdate.jms.MessagingParameters;
import static net.java.trueupdate.util.Objects.requireNonNull;

/**
 * JMS Update Agent Parameters.
 *
 * @author Christian Schlichtherle
 */
@Immutable
public final class JmsUpdateAgentParameters {

    private static final String CONFIGURATION = "META-INF/update/agent.xml";

    private final ApplicationParameters applicationParameters;
    private final MessagingParameters messagingParameters;

    JmsUpdateAgentParameters(final Builder b) {
        this.applicationParameters = requireNonNull(b.applicationParameters);
        this.messagingParameters = requireNonNull(b.messagingParameters);
    }

    /**
     * Loads JMS Update Agent Parameters from the configuration resource
     * file with the name {@code META-INF/update/agent.xml}.
     */
    public static JmsUpdateAgentParameters load() {
        return load(net.java.trueupdate.util.Resources.locate(CONFIGURATION));
    }

    static JmsUpdateAgentParameters load(final URL source) {
        try {
            return parse(JAXB.unmarshal(source, JmsUpdateAgentParametersDto.class));
        } catch (Exception ex) {
            throw new ServiceConfigurationError(String.format(
                    "Failed to load configuration from %s .", source),
                    ex);
        }
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
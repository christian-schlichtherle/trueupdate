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
import net.java.trueupdate.agent.core.*;
import net.java.trueupdate.agent.jms.ci.JmsUpdateAgentParametersCi;
import net.java.trueupdate.jms.MessagingParameters;
import static net.java.trueupdate.util.Objects.*;
import net.java.trueupdate.util.builder.AbstractBuilder;

/**
 * JMS update agent parameters.
 *
 * @author Christian Schlichtherle
 */
@Immutable
public final class JmsUpdateAgentParameters {

    private static final String CONFIGURATION = "update/agent.xml";

    private final ApplicationParameters application;
    private final TimerParameters subscriptionTimer;
    private final MessagingParameters messaging;

    JmsUpdateAgentParameters(final Builder<?> b) {
        this.application = requireNonNull(b.application);
        this.subscriptionTimer = null != b.subscriptionTimer
                ? b.subscriptionTimer : TimerParameters.builder().build();
        this.messaging = requireNonNull(b.messaging);
    }

    /**
     * Loads JMS update agent parameters from the configuration resource
     * file with the name {@code update/agent.xml}.
     */
    public static JmsUpdateAgentParameters load() {
        return load(net.java.trueupdate.util.Resources.locate(CONFIGURATION));
    }

    static JmsUpdateAgentParameters load(final URL source) {
        try {
            return parse(JAXB.unmarshal(source,
                                        JmsUpdateAgentParametersCi.class));
        } catch (Exception ex) {
            throw new ServiceConfigurationError(String.format(
                    "Failed to load configuration from %s .", source),
                    ex);
        }
    }

    /** Parses the given configuration item. */
    public static JmsUpdateAgentParameters parse(JmsUpdateAgentParametersCi ci) {
        return builder().parse(ci).build();
    }

    /** Returns a new builder for JMS update agent parameters. */
    public static Builder<Void> builder() { return new Builder<Void>(); }

    /** Returns the application parameters. */
    public ApplicationParameters application() { return application; }

    /**
     * Returns the timer parameters for the initial delay for the subscription
     * to the update manager.
     */
    public TimerParameters subscriptionTimer() { return subscriptionTimer; }

    /** Returns the messaging parameters. */
    public MessagingParameters messaging() { return messaging; }

    /**
     * A builder for JMS update agent parameters.
     *
     * @param <P> The type of the parent builder, if defined.
     */
    @SuppressWarnings("PackageVisibleField")
    public static class Builder<P> extends AbstractBuilder<P> {

        @CheckForNull ApplicationParameters application;
        @CheckForNull TimerParameters subscriptionTimer;
        @CheckForNull MessagingParameters messaging;

        protected Builder() { }

        /** Selectively parses the given configuration item. */
        public final Builder<P> parse(final JmsUpdateAgentParametersCi ci) {
            if (null != ci.application)
                application = ApplicationParameters.parse(ci.application);
            if (null != ci.subscriptionTimer)
                subscriptionTimer = TimerParameters.parse(ci.subscriptionTimer);
            if (null != ci.messaging)
                messaging = MessagingParameters.parse(ci.messaging);
            return this;
        }

        public final Builder<P> application(
                final @Nullable ApplicationParameters applicationParameters) {
            this.application = applicationParameters;
            return this;
        }

        public final Builder<P> subscriptionTimer(
                final @Nullable TimerParameters subscriptionDelay) {
            this.subscriptionTimer = subscriptionDelay;
            return this;
        }

        public final Builder<P> messaging(
                final @Nullable MessagingParameters messagingParameters) {
            this.messaging = messagingParameters;
            return this;
        }

        @Override public final JmsUpdateAgentParameters build() {
            return new JmsUpdateAgentParameters(this);
        }
    } // Builder
}

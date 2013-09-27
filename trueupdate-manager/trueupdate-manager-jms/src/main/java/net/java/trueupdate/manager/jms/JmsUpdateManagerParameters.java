/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.jms;

import java.net.*;
import java.util.ServiceConfigurationError;
import javax.annotation.*;
import javax.annotation.concurrent.Immutable;
import javax.xml.bind.JAXB;
import net.java.trueupdate.jms.MessagingParameters;
import net.java.trueupdate.manager.jms.dto.JmsUpdateManagerParametersDto;
import net.java.trueupdate.manager.spec.*;
import static net.java.trueupdate.util.Objects.requireNonNull;
import net.java.trueupdate.util.builder.AbstractBuilder;

/**
 * JMS update manager parameters.
 *
 * @author Christian Schlichtherle
 */
@Immutable
public final class JmsUpdateManagerParameters {

    private static final String CONFIGURATION = "META-INF/update/manager.xml";

    private final UpdateServiceParameters updateService;
    private final TimerParameters updateTimer;
    private final MessagingParameters messaging;

    JmsUpdateManagerParameters(final Builder<?> b) {
        this.updateService = requireNonNull(b.updateService);
        this.updateTimer = requireNonNull(b.updateTimer);
        this.messaging = requireNonNull(b.messaging);
    }

    /**
     * Loads JMS update manager parameters from the configuration resource
     * file with the name {@code META-INF/update/manager.xml}.
     */
    public static JmsUpdateManagerParameters load() {
        return load(net.java.trueupdate.util.Resources.locate(CONFIGURATION));
    }

    static JmsUpdateManagerParameters load(final URL source) {
        try {
            return parse(JAXB.unmarshal(source,
                                        JmsUpdateManagerParametersDto.class));
        } catch (Exception ex) {
            throw new ServiceConfigurationError(String.format(
                    "Failed to load configuration from %s .", source),
                    ex);
        }
    }

    /** Parses the given configuration item. */
    public static JmsUpdateManagerParameters parse(
            JmsUpdateManagerParametersDto ci) {
        return builder().parse(ci).build();
    }

    /** Returns a new builder for JMS update manager parameters. */
    public static Builder<Void> builder() { return new Builder<Void>(); }

    /** Returns the update service parameters. */
    public UpdateServiceParameters updateService() { return updateService; }

    /** Returns the timer parameters for checking for artifact updates. */
    public TimerParameters updateTimer() { return updateTimer; }

    /** Returns the messaging parameters. */
    public MessagingParameters messaging() { return messaging; }

    /**
     * A builder for JMS update manager parameters.
     *
     * @param <P> The type of the parent builder, if defined.
     */
    @SuppressWarnings("PackageVisibleField")
    public static class Builder<P> extends AbstractBuilder<P> {

        @CheckForNull UpdateServiceParameters updateService;
        @CheckForNull TimerParameters updateTimer;
        @CheckForNull MessagingParameters messaging;

        protected Builder() { }

        /** Selectively parses the given configuration item. */
        public final Builder<P> parse(final JmsUpdateManagerParametersDto ci) {
            if (null != ci.updateService)
                updateService = UpdateServiceParameters.parse(ci.updateService);
            if (null != ci.updateTimer)
                updateTimer = TimerParameters.parse(ci.updateTimer);
            if (null != ci.messaging)
                messaging = MessagingParameters.parse(ci.messaging);
            return this;
        }

        public final Builder<P> updateService(
                final @Nullable UpdateServiceParameters updateService) {
            this.updateService = updateService;
            return this;
        }

        public final Builder<P> updateTimer(
                final @Nullable TimerParameters updateTimer) {
            this.updateTimer = updateTimer;
            return this;
        }

        public final Builder<P> messaging(
                final @Nullable MessagingParameters messaging) {
            this.messaging = messaging;
            return this;
        }

        @Override public final JmsUpdateManagerParameters build() {
            return new JmsUpdateManagerParameters(this);
        }
    } // Builder
}

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
import net.java.trueupdate.manager.spec.TimerParameters;
import static net.java.trueupdate.util.Objects.requireNonNull;
import static net.java.trueupdate.util.SystemProperties.resolve;
import net.java.trueupdate.util.builder.AbstractBuilder;

/**
 * JMS Update Manager Parameters.
 *
 * @author Christian Schlichtherle
 */
@Immutable
public final class JmsUpdateManagerParameters {

    private static final String CONFIGURATION = "META-INF/update/manager.xml";

    private final URI updateServiceBaseUri;
    private final TimerParameters checkForUpdates;
    private final MessagingParameters messagingParameters;

    JmsUpdateManagerParameters(final Builder<?> b) {
        this.updateServiceBaseUri = requireNonNull(b.updateServiceBaseUri);
        this.checkForUpdates = requireNonNull(b.checkForUpdates);
        this.messagingParameters = requireNonNull(b.messagingParameters);
    }

    /**
     * Loads JMS Update Manager Parameters from the configuration resource
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

    /** Returns a new builder for update manager parameters. */
    public static Builder<Void> builder() { return new Builder<Void>(); }

    /** Returns the base URI of the update server. */
    public URI updateServiceBaseUri() { return updateServiceBaseUri; }

    /** Returns the timer parameters for checking for artifact updates. */
    public TimerParameters checkForUpdates() {
        return checkForUpdates;
    }

    /** Returns the messagingParameters parameters. */
    public MessagingParameters messagingParameters() {
        return messagingParameters;
    }

    /**
     * A builder for update manager parameters.
     *
     * @param <P> The type of the parent builder, if defined.
     */
    @SuppressWarnings("PackageVisibleField")
    public static class Builder<P> extends AbstractBuilder<P> {

        @CheckForNull URI updateServiceBaseUri;
        TimerParameters checkForUpdates;
        @CheckForNull MessagingParameters messagingParameters;

        protected Builder() { }

        /** Selectively parses the given configuration item. */
        public final Builder<P> parse(final JmsUpdateManagerParametersDto ci) {
            if (null != ci.updateServiceBaseUri)
                updateServiceBaseUri = URI.create(ensureEndsWithSlash(resolve(
                        ci.updateServiceBaseUri)));
            if (null != ci.checkForUpdates)
                checkForUpdates = TimerParameters.parse(ci.checkForUpdates);
            if (null != ci.messaging)
                messagingParameters = MessagingParameters.parse(ci.messaging);
            return this;
        }

        private static String ensureEndsWithSlash(String string) {
            return string.endsWith("/") ? string : string + "/";
        }

        public final Builder<P> updateServiceBaseUri(
                final @Nullable URI updateServiceBaseUri) {
            this.updateServiceBaseUri = updateServiceBaseUri;
            return this;
        }

        public final Builder<P> checkForUpdates(
                final @Nullable TimerParameters checkForUpdates) {
            this.checkForUpdates = checkForUpdates;
            return this;
        }

        public final Builder<P> messagingParameters(
                final @Nullable MessagingParameters messagingParameters) {
            this.messagingParameters = messagingParameters;
            return this;
        }

        @Override public final JmsUpdateManagerParameters build() {
            return new JmsUpdateManagerParameters(this);
        }
    } // Builder
}

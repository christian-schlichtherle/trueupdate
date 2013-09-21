/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.jms;

import java.net.URI;
import javax.annotation.*;
import javax.annotation.concurrent.Immutable;
import net.java.trueupdate.jms.MessagingParameters;
import net.java.trueupdate.manager.jms.dto.JmsUpdateManagerParametersDto;
import static net.java.trueupdate.util.Objects.requireNonNull;
import static net.java.trueupdate.util.SystemProperties.resolve;

/**
 * JMS Update Manager Parameters.
 *
 * @author Christian Schlichtherle
 */
@Immutable
public final class JmsUpdateManagerParameters {

    private final URI updateServiceBaseUri;
    private final int checkUpdatesIntervalMinutes;
    private final MessagingParameters messagingParameters;

    JmsUpdateManagerParameters(final Builder b) {
        this.updateServiceBaseUri = requireNonNull(b.updateServiceBaseUri);
        this.checkUpdatesIntervalMinutes = requirePositive(b.checkUpdatesIntervalMinutes);
        this.messagingParameters = requireNonNull(b.messagingParameters);
    }

    private static int requirePositive(final int i) {
        if (0 >= i) throw new IllegalArgumentException();
        return i;
    }

    /** Parses the given configuration item. */
    public static JmsUpdateManagerParameters parse(JmsUpdateManagerParametersDto ci) {
        return builder().parse(ci).build();
    }

    /** Returns a new builder for update manager parameters. */
    public static Builder builder() { return new Builder(); }

    /** Returns the base URI of the update server. */
    public URI updateServiceBaseUri() { return updateServiceBaseUri; }

    /** Returns the interval for checking for artifact updates in minutes. */
    public int checkUpdatesIntervalMinutes() { return checkUpdatesIntervalMinutes; }

    /** Returns the messagingParameters parameters. */
    public MessagingParameters messagingParameters() { return messagingParameters; }

    /** A builder for update manager parameters. */
    @SuppressWarnings({ "PackageVisibleField", "PackageVisibleInnerClass" })
    public static class Builder {

        @CheckForNull URI updateServiceBaseUri;
        int checkUpdatesIntervalMinutes;
        @CheckForNull MessagingParameters messagingParameters;

        /** Selectively parses the given configuration item. */
        public Builder parse(final JmsUpdateManagerParametersDto ci) {
            if (null != ci.updateServiceBaseUri)
                updateServiceBaseUri = URI.create(ensureEndsWithSlash(resolve(
                        ci.updateServiceBaseUri)));
            if (null != ci.checkUpdatesIntervalMinutes)
                checkUpdatesIntervalMinutes = Integer.parseInt(resolve(
                        ci.checkUpdatesIntervalMinutes));
            if (null != ci.messaging)
                messagingParameters = MessagingParameters.parse(ci.messaging);
            return this;
        }

        private static String ensureEndsWithSlash(String string) {
            return string.endsWith("/") ? string : string + "/";
        }

        public Builder updateServiceBaseUri(
                final @Nullable URI updateServiceBaseUri) {
            this.updateServiceBaseUri = updateServiceBaseUri;
            return this;
        }

        public Builder checkUpdatesIntervalMinutes(
                final int checkUpdatesIntervalMinutes) {
            this.checkUpdatesIntervalMinutes = checkUpdatesIntervalMinutes;
            return this;
        }

        public Builder messagingParameters(
                final @Nullable MessagingParameters messagingParameters) {
            this.messagingParameters = messagingParameters;
            return this;
        }

        public JmsUpdateManagerParameters build() {
            return new JmsUpdateManagerParameters(this);
        }
    } // Builder
}

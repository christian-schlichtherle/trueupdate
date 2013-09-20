/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.servlets;

import java.net.URI;
import javax.annotation.*;
import javax.annotation.concurrent.Immutable;
import net.java.trueupdate.jms.MessagingParameters;
import net.java.trueupdate.manager.servlets.dto.UpdateManagerParametersDto;
import static net.java.trueupdate.util.Objects.requireNonNull;
import static net.java.trueupdate.util.SystemProperties.resolve;

/**
 * Update Manager Parameters.
 *
 * @author Christian Schlichtherle
 */
@Immutable
final class UpdateManagerParameters {

    private final URI updateServiceBaseUri;
    private final int checkUpdatesIntervalMinutes;
    private final MessagingParameters messagingParameters;

    UpdateManagerParameters(final Builder b) {
        this.updateServiceBaseUri = requireNonNull(b.updateServiceBaseUri);
        this.checkUpdatesIntervalMinutes = requirePositive(b.checkUpdatesIntervalMinutes);
        this.messagingParameters = requireNonNull(b.messagingParameters);
    }

    private static int requirePositive(final int i) {
        if (0 >= i) throw new IllegalArgumentException();
        return i;
    }

    /** Returns a new builder for update manager parameters. */
    static Builder builder() { return new Builder(); }

    /** Returns the base URI of the update server. */
    URI updateServiceBaseUri() { return updateServiceBaseUri; }

    /** Returns the interval for checking for artifact updates in minutes. */
    int checkUpdatesIntervalMinutes() { return checkUpdatesIntervalMinutes; }

    /** Returns the messagingParameters parameters. */
    MessagingParameters messagingParameters() { return messagingParameters; }

    /** A builder for update manager parameters. */
    @SuppressWarnings({ "PackageVisibleField", "PackageVisibleInnerClass" })
    static class Builder {

        @CheckForNull URI updateServiceBaseUri;
        int checkUpdatesIntervalMinutes;
        @CheckForNull MessagingParameters messagingParameters;

        /** Parses the given configuration item. */
        Builder parse(final UpdateManagerParametersDto ci) {
            updateServiceBaseUri = parseUri(
                    ci.updateServiceBaseUri,
                    updateServiceBaseUri);
            checkUpdatesIntervalMinutes = parseInt(
                    ci.checkUpdatesIntervalMinutes,
                    checkUpdatesIntervalMinutes);
            if (null != ci.messaging)
                messagingParameters = MessagingParameters
                        .builder()
                        .parseNaming(ci.naming)
                        .parseMessaging(ci.messaging)
                        .build();
            return this;
        }

        private static @Nullable URI parseUri(@CheckForNull String string,
                                              @Nullable URI defaultValue) {
            return null == string ? defaultValue
                    : URI.create(ensureEndsWithSlash(resolve(string)));
        }

        private static String ensureEndsWithSlash(String string) {
            return string.endsWith("/") ? string : string + "/";
        }

        private static int parseInt(@CheckForNull String string,
                                   int defaultValue) {
            return null == string ? defaultValue
                    : Integer.parseInt(resolve(string));
        }

        UpdateManagerParameters build() {
            return new UpdateManagerParameters(this);
        }
    } // Builder
}

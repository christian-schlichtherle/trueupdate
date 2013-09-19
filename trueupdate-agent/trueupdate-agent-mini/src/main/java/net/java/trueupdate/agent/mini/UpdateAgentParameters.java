/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.agent.mini;

import javax.annotation.CheckForNull;
import net.java.trueupdate.agent.mini.config.*;
import net.java.trueupdate.agent.spec.UpdateAgentListener;
import net.java.trueupdate.agent.spec.ApplicationParameters;
import net.java.trueupdate.artifact.spec.ArtifactDescriptor;
import net.java.trueupdate.jms.MessagingParameters;
import net.java.trueupdate.manager.spec.ApplicationDescriptor;
import static net.java.trueupdate.util.Objects.requireNonNull;
import net.java.trueupdate.util.SystemProperties;

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
        Builder parse(final UpdateAgentConfiguration config) {
            applicationParameters = parse(
                        ApplicationParameters.builder(),
                        config.application
                    ).build();
            messagingParameters = MessagingParameters
                    .builder()
                    .parseNaming(config.naming)
                    .parseMessaging(config.messaging)
                    .build();
            return this;
        }

        private static <P> ApplicationParameters.Builder<P> parse(
                ApplicationParameters.Builder<P> builder,
                ApplicationConfiguration config) {
            return parse(builder.applicationDescriptor(), config).inject()
                    .applicationListener(listener(config.listenerClass))
                    .updateLocation(resolve(config.updateLocation));
        }

        private static <P> ApplicationDescriptor.Builder<P> parse(
                ApplicationDescriptor.Builder<P> builder,
                ApplicationConfiguration config) {
            return parse(builder.artifactDescriptor(), config.artifact).inject()
                    .currentLocation(resolve(config.currentLocation));
        }

        private static <P> ArtifactDescriptor.Builder<P> parse(
                ArtifactDescriptor.Builder<P> builder,
                ArtifactConfiguration config) {
            return builder
                    .groupId(resolve(config.groupId))
                    .artifactId(resolve(config.artifactId))
                    .version(resolve(config.version))
                    .classifier(resolve(config.classifier))
                    .extension(resolve(config.extension));
        }

        private static String resolve(String string) {
            return null == string ? null : SystemProperties.resolve(string);
        }

        @SuppressWarnings("unchecked")
        private static UpdateAgentListener listener(final String className) {
            try {
                return (UpdateAgentListener) Thread
                        .currentThread()
                        .getContextClassLoader()
                        .loadClass(resolve(className))
                        .newInstance();
            } catch (Exception ex) {
                throw new IllegalArgumentException(ex);
            }
        }

        UpdateAgentParameters build() {
            return new UpdateAgentParameters(this);
        }
    } // Builder
}

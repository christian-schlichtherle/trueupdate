/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.server.maven;

import java.net.URL;
import java.util.ServiceConfigurationError;
import javax.annotation.*;
import javax.annotation.concurrent.Immutable;
import javax.xml.bind.JAXB;
import net.java.trueupdate.artifact.maven.*;
import net.java.trueupdate.artifact.spec.ArtifactResolver;
import net.java.trueupdate.server.maven.ci.MavenUpdateServerParametersCi;
import net.java.trueupdate.util.builder.AbstractBuilder;

/**
 * Maven update server parameters.
 *
 * @author Christian Schlichtherle
 */
@Immutable
public final class MavenUpdateServerParameters {

    private static final String CONFIGURATION = "update/server.xml";

    private final ArtifactResolver artifactResolver;

    MavenUpdateServerParameters(final Builder<?> b) {
        this.artifactResolver = new MavenArtifactResolver(b.mavenParameters);
    }

    /**
     * Loads Maven update server parameters from the configuration resource
     * file with the name {@code update/server.xml}.
     */
    public static MavenUpdateServerParameters load() {
        return load(net.java.trueupdate.util.Resources.locate(CONFIGURATION));
    }

    static MavenUpdateServerParameters load(final URL source) {
        try {
            return parse(JAXB.unmarshal(source,
                                        MavenUpdateServerParametersCi.class));
        } catch (Exception ex) {
            throw new ServiceConfigurationError(String.format(
                    "Failed to load configuration from %s .", source),
                    ex);
        }
    }

    /** Parses the given configuration item. */
    public static MavenUpdateServerParameters parse(MavenUpdateServerParametersCi ci) {
        return builder().parse(ci).build();
    }

    /** Returns a new builder for maven update server parameters. */
    public static Builder<Void> builder() { return new Builder<Void>(); }

    /** Returns the artifact resolver. */
    public ArtifactResolver artifactResolver() { return artifactResolver; }

    /**
     * A builder for maven update server parameters.
     *
     * @param <P> The type of the parent builder, if defined.
     */
    @SuppressWarnings("PackageVisibleField")
    public static class Builder<P> extends AbstractBuilder<P> {

        @CheckForNull MavenParameters mavenParameters;

        protected Builder() { }

        /** Selectively parses the given configuration item. */
        public final Builder<P> parse(final MavenUpdateServerParametersCi ci) {
            if (null != ci.repositories)
                mavenParameters = MavenParameters.parse(ci.repositories);
            return this;
        }

        public final Builder<P> mavenParameters(
                final @Nullable MavenParameters mavenParameters) {
            this.mavenParameters = mavenParameters;
            return this;
        }

        @Override public final MavenUpdateServerParameters build() {
            return new MavenUpdateServerParameters(this);
        }
    } // Builder
}

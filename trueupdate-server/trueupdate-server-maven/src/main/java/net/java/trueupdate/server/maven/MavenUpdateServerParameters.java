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
import net.java.trueupdate.server.maven.dto.MavenUpdateServerParametersDto;
import static net.java.trueupdate.util.Objects.*;

/**
 * Maven Update Server Parameters.
 *
 * @author Christian Schlichtherle
 */
@Immutable
public final class MavenUpdateServerParameters {

    private static final String CONFIGURATION = "META-INF/update/server.xml";

    private final ArtifactResolver artifactResolver;

    MavenUpdateServerParameters(final Builder b) {
        this.artifactResolver = requireNonNull(b.artifactResolver);
    }

    /**
     * Loads Maevn Update Server Parameters from the configuration resource
     * file with the name {@code META-INF/update/server.xml}.
     */
    public static MavenUpdateServerParameters load() {
        return load(net.java.trueupdate.util.Resources.locate(CONFIGURATION));
    }

    static MavenUpdateServerParameters load(final URL source) {
        try {
            return parse(JAXB.unmarshal(source, MavenUpdateServerParametersDto.class));
        } catch (Exception ex) {
            throw new ServiceConfigurationError(String.format(
                    "Failed to load configuration from %s .", source),
                    ex);
        }
    }

    /** Parses the given configuration item. */
    public static MavenUpdateServerParameters parse(MavenUpdateServerParametersDto ci) {
        return builder().parse(ci).build();
    }

    /** Returns a new builder for update server parameters. */
    public static Builder builder() { return new Builder(); }

    public ArtifactResolver artifactResolver() { return artifactResolver; }

    /** A builder for update server parameters. */
    @SuppressWarnings({ "PackageVisibleField", "PackageVisibleInnerClass" })
    public static class Builder {

        @CheckForNull ArtifactResolver artifactResolver;

        /** Selectively parses the given configuration item. */
        public Builder parse(final MavenUpdateServerParametersDto ci) {
            if (null != ci.repositories)
                artifactResolver = new MavenArtifactResolver(MavenParameters
                        .parse(ci.repositories));
            return this;
        }

        public Builder artifactResolver(
                final @Nullable ArtifactResolver artifactResolver) {
            this.artifactResolver = artifactResolver;
            return this;
        }

        public MavenUpdateServerParameters build() {
            return new MavenUpdateServerParameters(this);
        }
    } // Builder
}

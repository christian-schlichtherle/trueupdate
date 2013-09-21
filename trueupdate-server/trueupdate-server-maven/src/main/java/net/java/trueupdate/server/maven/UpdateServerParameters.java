/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.server.maven;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import net.java.trueupdate.artifact.maven.*;
import net.java.trueupdate.artifact.spec.ArtifactResolver;
import net.java.trueupdate.server.maven.dto.UpdateServerParametersDto;
import static net.java.trueupdate.util.Objects.*;

/**
 * Update Server parameters.
 *
 * @author Christian Schlichtherle
 */
@Immutable
final class UpdateServerParameters {

    private final ArtifactResolver artifactResolver;

    UpdateServerParameters(final Builder b) {
        this.artifactResolver = requireNonNull(b.artifactResolver);
    }

    /** Parses the given configuration item. */
    public static UpdateServerParameters parse(UpdateServerParametersDto ci) {
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
        public Builder parse(final UpdateServerParametersDto ci) {
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

        public UpdateServerParameters build() {
            return new UpdateServerParameters(this);
        }
    } // Builder
}

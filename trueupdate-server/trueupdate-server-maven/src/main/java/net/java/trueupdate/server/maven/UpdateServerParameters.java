/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.server.maven;

import javax.annotation.CheckForNull;
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

    /** Returns a new builder for update server parameters. */
    static Builder builder() { return new Builder(); }

    ArtifactResolver artifactResolver() { return artifactResolver; }

    /** A builder for update server parameters. */
    @SuppressWarnings({ "PackageVisibleField", "PackageVisibleInnerClass" })
    static class Builder {

        @CheckForNull MavenArtifactResolver artifactResolver;

        /** Parses the given configuration item. */
        Builder parse(final UpdateServerParametersDto ci) {
            artifactResolver = new MavenArtifactResolver(MavenParameters
                    .builder()
                    .parse(ci.repositories)
                    .build());
            return this;
        }

        UpdateServerParameters build() {
            return new UpdateServerParameters(this);
        }
    } // Builder
}

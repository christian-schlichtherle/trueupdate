/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.artifact.maven;

import java.io.File;
import java.util.*;
import javax.annotation.*;
import javax.annotation.concurrent.Immutable;
import net.java.trueupdate.artifact.maven.ci.*;
import static net.java.trueupdate.util.Objects.*;
import static net.java.trueupdate.util.SystemProperties.resolve;
import net.java.trueupdate.util.builder.*;
import org.eclipse.aether.repository.*;

/**
 * Maven parameters.
 *
 * @author Christian Schlichtherle
 */
@Immutable
public final class MavenParameters {

    private final LocalRepository local;
    private final List<RemoteRepository> remotes;

    @SuppressWarnings("unchecked")
    MavenParameters(final Builder<?> b) {
        this.local = requireNonNull(b.local);
        this.remotes = b.remotes.build();
    }

    /** Parses the given configuration item. */
    public static MavenParameters parse(MavenParametersCi ci) {
        return builder().parse(ci).build();
    }

    /** Returns a new builder for Maven parameters. */
    public static Builder<Void> builder() { return new Builder<Void>(); }

    /** Returns the local repository. */
    public LocalRepository localRepository() { return local; }

    /** Returns an immutable list of the remote repositories. */
    @SuppressWarnings("ReturnOfCollectionOrArrayField")
    public List<RemoteRepository> remoteRepositories() { return remotes; }

    /**
     * A builder for Maven parameters.
     *
     * @param <P> The type of the parent builder, if defined.
     */
    @SuppressWarnings("PackageVisibleField")
    public static class Builder<P> extends AbstractBuilder<P> {

        @CheckForNull LocalRepository local;
        final ImmutableListBuilder<RemoteRepository, Void>
                remotes = ImmutableListBuilder.create();

        protected Builder() { }

        /** Selectively parses the given configuration item. */
        public final Builder<P> parse(final MavenParametersCi ci) {
            if (null != ci.local) local = local(ci.local);
            if (null != ci.remotes) addRemotes(ci.remotes);
            return this;
        }

        private static LocalRepository local(final LocalRepositoryCi ci) {
            return new LocalRepository(
                    new File(resolve(nonNullOr(ci.directory, "${user.home}/.m2"))),
                    resolve(ci.type, null));
        }

        private void addRemotes(final RemoteRepositoryCi[] cis) {
            final int l = cis.length;
            for (int i = 0; i < l; i++) {
                final RemoteRepositoryCi ci = cis[i];
                remotes.add(new RemoteRepository.Builder(
                        resolve(ci.id, null),
                        resolve(ci.type, null),
                        resolve(ci.url, "http://repo1.maven.org/maven2/")
                        ).build());
            }
        }

        public final Builder<P> localRepository(
                final @Nullable LocalRepository local) {
            this.local = local;
            return this;
        }

        public final ImmutableListBuilder<RemoteRepository, Builder<P>> remoteRepositories() {
            return new ImmutableListBuilder<RemoteRepository, Builder<P>>() {
                @Override public Builder<P> inject() {
                    return remoteRepositories(build());
                }
            };
        }

        public final Builder<P> remoteRepositories(
                final List<RemoteRepository> remotes) {
            this.remotes.setAll(remotes);
            return this;
        }

        @Override public final MavenParameters build() {
            return new MavenParameters(this);
        }
    } // Builder
}

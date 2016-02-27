/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.artifact.maven;

import net.java.trueupdate.artifact.maven.ci.LocalRepositoryCi;
import net.java.trueupdate.artifact.maven.ci.MavenParametersCi;
import net.java.trueupdate.artifact.maven.ci.RemoteRepositoryCi;
import net.java.trueupdate.util.builder.AbstractBuilder;
import net.java.trueupdate.util.builder.ImmutableListBuilder;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RemoteRepository;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import java.io.File;
import java.util.List;

import static java.util.Objects.requireNonNull;
import static net.java.trueupdate.util.Objects.nonNullOr;
import static net.java.trueupdate.util.SystemProperties.resolve;

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
                    new File(resolve(nonNullOr(ci.directory, "${user.home}/.m2/repository"))),
                    resolve(ci.type, null));
        }

        private void addRemotes(final RemoteRepositoryCi[] cis) {
            for (final RemoteRepositoryCi ci : cis) {
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

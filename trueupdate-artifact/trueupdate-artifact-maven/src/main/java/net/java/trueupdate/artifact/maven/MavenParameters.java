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
import static net.java.trueupdate.util.Objects.requireNonNull;
import static net.java.trueupdate.util.SystemProperties.resolve;
import org.eclipse.aether.repository.*;

/**
 * Aether parameters.
 *
 * @author Christian Schlichtherle
 */
@Immutable
public final class MavenParameters {

    private final LocalRepository local;
    private final List<RemoteRepository> remotes;

    MavenParameters(final Builder<?> b) {
        this.local = requireNonNull(b.local);
        this.remotes = Collections.unmodifiableList(
                new ArrayList<RemoteRepository>(b.remotes));
    }

    /** Returns a new builder for Aether parameters. */
    public static Builder<Void> builder() { return new Builder<Void>(); }

    /** Returns the local repository. */
    public LocalRepository localRepository() { return local; }

    /** Returns the immutable list of remote repositories. */
    @SuppressWarnings("ReturnOfCollectionOrArrayField")
    public List<RemoteRepository> remoteRepositories() { return remotes; }

    /**
     * A builder for Aether parameters.
     *
     * @param <P> The type of the parent builder.
     */
    @SuppressWarnings("PackageVisibleField")
    public static class Builder<P> {

        @CheckForNull LocalRepository local;
        @CheckForNull List<RemoteRepository> remotes;

        protected Builder() { }

        public Builder<P> parse(MavenCi ci) {
            return parse(ci.local).parse(ci.remotes);
        }

        private Builder<P> parse(final LocalRepositoryCi ci) {
            local = new LocalRepository(new File(resolve(ci.basedir)),
                                                 resolve(ci.type));
            return this;
        }

        private Builder<P> parse(final List<RemoteRepositoryCi> cis) {
            remotes = new ArrayList<RemoteRepository>();
            for (RemoteRepositoryCi ci : cis)
                remotes.add(new RemoteRepository.Builder(
                        resolve(ci.id),
                        resolve(ci.type),
                        resolve(ci.url)).build());
            return this;
        }

        public Builder<P> localRepository(
                final @Nullable LocalRepository local) {
            this.local = local;
            return this;
        }

        public Builder<P> remoteRepositories(
                final @Nullable RemoteRepository... remotes) {
            this.remotes = null == remotes ? null : Arrays.asList(remotes);
            return this;
        }

        @SuppressWarnings("AssignmentToCollectionOrArrayFieldFromParameter")
        public Builder<P> remoteRepositories(
                final @Nullable List<RemoteRepository> remotes) {
            this.remotes = remotes;
            return this;
        }

        public MavenParameters build() { return new MavenParameters(this); }

        /**
         * Injects the product of this builder into the parent builder, if
         * defined.
         *
         * @throws IllegalStateException if there is no parent builder defined.
         */
        public P inject() {
            throw new java.lang.IllegalStateException("No parent builder defined.");
        }

    } // Builder
}

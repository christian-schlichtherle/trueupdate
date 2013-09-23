/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.artifact.maven;

import java.io.File;
import java.util.*;
import static java.util.Collections.*;
import javax.annotation.*;
import javax.annotation.concurrent.Immutable;
import net.java.trueupdate.artifact.maven.dto.*;
import static net.java.trueupdate.util.Objects.*;
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

    @SuppressWarnings("unchecked")
    MavenParameters(final Builder<?> b) {
        this.local = requireNonNull(b.localRepositories);
        this.remotes = null == b.remoteRepositories
                ? EMPTY_LIST
                : unmodifiableList(Arrays.asList(b.remoteRepositories.clone()));
    }

    /** Parses the given configuration item. */
    public static MavenParameters parse(MavenParametersDto ci) {
        return builder().parse(ci).build();
    }

    /** Returns a new builder for Aether parameters. */
    public static Builder<Void> builder() { return new Builder<Void>(); }

    /** Returns the localRepositories repository. */
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

        @CheckForNull LocalRepository localRepositories;
        @CheckForNull RemoteRepository[] remoteRepositories;

        protected Builder() { }

        /** Selectively parses the given configuration item. */
        public Builder<P> parse(MavenParametersDto ci) {
            if (null != ci.local) localRepositories = local(ci.local);
            if (null != ci.remotes) remoteRepositories = remotes(ci.remotes);
            return this;
        }

        private static LocalRepository local(final LocalRepositoryDto ci) {
            return new LocalRepository(
                    new File(resolve(nonNullOr(ci.directory, "${user.home}/.m2"))),
                    resolve(ci.type, null));
        }

        private static RemoteRepository[] remotes(
                final RemoteRepositoryDto[] cis) {
            final int l = cis.length;
            final RemoteRepository[] remotes = new RemoteRepository[l];
            for (int i = 0; i < l; i++) {
                final RemoteRepositoryDto ci = cis[i];
                remotes[i] = new RemoteRepository.Builder(
                        resolve(ci.id, null),
                        resolve(ci.type, null),
                        resolve(ci.url, "http://repo1.maven.org/maven2/")).build();
            }
            return remotes;
        }

        public Builder<P> localRepository(
                final @Nullable LocalRepository local) {
            this.localRepositories = local;
            return this;
        }

        @SuppressWarnings("AssignmentToCollectionOrArrayFieldFromParameter")
        public Builder<P> remoteRepositories(
                final @Nullable RemoteRepository... remotes) {
            this.remoteRepositories = remotes;
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

/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.maven;

import java.io.File;
import java.util.*;
import javax.annotation.*;
import javax.annotation.concurrent.Immutable;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import net.java.trueupdate.core.util.SystemProperties;
import net.java.trueupdate.maven.model.*;
import org.eclipse.aether.repository.*;

/**
 * Adapts a maven artifact repository to a repositories model so that JAXB can
 * marshall / unmarshall it.
 *
 * @author Christian Schlichtherle
 */
@Immutable
public final class MavenArtifactRepositoryAdapter
extends XmlAdapter<Repositories, MavenArtifactRepository> {

    @Override public @Nullable
    MavenArtifactRepository unmarshal(
            final @CheckForNull Repositories repositories) {
        try {
            return null == repositories
                    ? null
                    : new MavenArtifactRepository(
                        localRepository(repositories.local),
                        remoteRepositories(repositories.remotes));
        } catch (NullPointerException ex) {
            throw new IllegalArgumentException("Invalid repositories model.", ex);
        }
    }

    private static LocalRepository localRepository(Local local) {
        return new LocalRepository(new File(replace(local.basedir)),
                replace(local.type));
    }

    private static List<RemoteRepository> remoteRepositories(
            final List<Remote> remotes) {
        final List<RemoteRepository>
                remoteRepositories = new ArrayList<>(remotes.size());
        for (Remote remote : remotes)
            remoteRepositories.add(remoteRepository(remote));
        return remoteRepositories;
    }

    private static RemoteRepository remoteRepository(Remote remote) {
        return new RemoteRepository
                .Builder(replace(remote.id), replace(remote.type),
                         replace(remote.url))
                .build();
    }

    @Override public @Nullable Repositories marshal(
            @CheckForNull MavenArtifactRepository mavenArtifactRepository) {
        return null == mavenArtifactRepository
                ? null
                : new Repositories(
                    local(mavenArtifactRepository.local),
                    remotes(mavenArtifactRepository.remotes));
    }

    private static Local local(LocalRepository localRepository) {
        return new Local(
                nonEmptyOrNull(localRepository.getBasedir().getPath()),
                nonEmptyOrNull(localRepository.getContentType()));
    }

    private static List<Remote> remotes(
            final List<RemoteRepository> remoteRepositories) {
        final List<Remote> remotes = new ArrayList<>(remoteRepositories.size());
        for (RemoteRepository remoteRepository : remoteRepositories)
            remotes.add(remote(remoteRepository));
        return remotes;
    }

    private static Remote remote(RemoteRepository remoteRepository) {
        return new Remote(
                nonEmptyOrNull(remoteRepository.getId()),
                nonEmptyOrNull(remoteRepository.getContentType()),
                nonEmptyOrNull(remoteRepository.getUrl()));
    }

    private static @Nullable String nonEmptyOrNull(String string) {
        return string.isEmpty() ? null : string;
    }

    private static @Nullable String replace(final @CheckForNull String string) {
        return null == string ? null : SystemProperties.resolve(string);
    }
}

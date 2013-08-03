/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.maven;

import com.stimulus.archiva.update.maven.model.*;
import java.io.File;
import java.util.*;
import javax.annotation.*;
import javax.annotation.concurrent.Immutable;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import org.eclipse.aether.repository.*;

/**
 * Adapts a maven artifact resolver to a repositories model so that JAXB can
 * marshall / unmarshall it.
 *
 * @author Christian Schlichtherle
 */
@Immutable
public final class MavenArtifactResolverAdapter
extends XmlAdapter<Repositories, MavenArtifactResolver> {

    @Override public @Nullable MavenArtifactResolver unmarshal(
            final @CheckForNull Repositories repositories) {
        return null == repositories
                ? null
                : new MavenArtifactResolver(
                    localRepository(repositories.local),
                    remoteRepositories(repositories.remotes));
    }

    private static LocalRepository localRepository(Local local) {
        return new LocalRepository(new File(local.basedir), local.type);
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
                .Builder(remote.id, remote.type, remote.url)
                .build();
    }

    @Override public @Nullable Repositories marshal(
            @CheckForNull MavenArtifactResolver mavenArtifactResolver) {
        return null == mavenArtifactResolver
                ? null
                : new Repositories(
                    local(mavenArtifactResolver.local),
                    remotes(mavenArtifactResolver.remotes));
    }

    private static Local local(LocalRepository localRepository) {
        return new Local(
                nonEmptyOrNull(localRepository.getBasedir().getPath()),
                nonEmptyOrNull(localRepository.getContentType()));
    }

    private static List<Remote> remotes(final List<RemoteRepository> remoteRepositories) {
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
}

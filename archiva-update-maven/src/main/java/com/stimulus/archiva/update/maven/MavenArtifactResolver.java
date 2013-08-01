/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.maven;

import com.stimulus.archiva.update.core.artifact.*;
import static com.stimulus.archiva.update.maven.ArtifactConverters.*;
import java.io.File;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.*;
import static java.util.Arrays.asList;
import javax.annotation.CheckForNull;
import javax.annotation.concurrent.ThreadSafe;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.*;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.connector.file.FileRepositoryConnectorFactory;
import org.eclipse.aether.connector.wagon.*;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.repository.*;
import org.eclipse.aether.resolution.*;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.locator.ServiceLocator;
import org.eclipse.aether.spi.log.LoggerFactory;
import org.eclipse.aether.version.Version;

/**
 * Resolves paths to described artifacts and their latest update by looking
 * them up in a local Maven repository and, optionally, some remote Maven
 * repositories.
 *
 * @author Christian Schlichtherle
 */
@ThreadSafe
public class MavenArtifactResolver implements ArtifactResolver {

    private volatile ServiceLocator serviceLocator;
    private volatile RepositorySystemSession repositorySystemSession;

    private final LocalRepository local;
    private final List<RemoteRepository> remotes;

    /**
     * Constructs a maven path resolver which uses the given local and remote
     * Maven repositories for finding a described artifact and its latest
     * update.
     *
     * @param local the local repository for artifacts.
     * @param remotes the remote repositories for artifacts.
     */
    public MavenArtifactResolver(
            final LocalRepository local,
            final RemoteRepository... remotes) {
        this.local = Objects.requireNonNull(local);
        this.remotes = Collections.unmodifiableList(asList(remotes));
    }

    @Override public File resolveArtifactFile(ArtifactDescriptor descriptor)
    throws RepositoryException {
        return resolveArtifact(descriptor).getFile();
    }

    private Artifact resolveArtifact(ArtifactDescriptor descriptor)
    throws RepositoryException {
        return resolveArtifact(artifact(descriptor));
    }

    private Artifact resolveArtifact(final Artifact artifact)
    throws RepositoryException {
        final ArtifactResult artifactResult =
                resolveArtifact(artifactRequest(artifact));
        final @CheckForNull Artifact resolved = artifactResult.getArtifact();
        if (null == resolved)
            throw addSuppressed(
                    new ArtifactResolutionException(asList(artifactResult),
                            "Artifact not found."),
                    artifactResult.getExceptions());
        return resolved;
    }

    @Override public ArtifactDescriptor resolveUpdateDescriptor(
            ArtifactDescriptor descriptor)
    throws RepositoryException {
        return descriptor(resolveUpdateArtifact(descriptor));
    }

    private Artifact resolveUpdateArtifact(ArtifactDescriptor descriptor)
    throws RepositoryException {
        try {
            return resolveUpdateArtifact(updateRangeArtifact(descriptor));
        } catch (final VersionRangeResolutionException ex) {
            // Try to throw a more specific exception, otherwise propagate ex.
            resolveArtifact(descriptor);
            throw ex;
        }
    }

    private Artifact resolveUpdateArtifact(final Artifact updateRangeArtifact)
    throws RepositoryException {
        final VersionRangeResult result =
                resolveVersionRange(versionRangeRequest(updateRangeArtifact));
        final @CheckForNull Version highestVersion =
                result.getHighestVersion();
        if (null == highestVersion) {
            throw addSuppressed(
                    new VersionRangeResolutionException(result,
                            "Update artifact not found."),
                    result.getExceptions());
        }
        return updateRangeArtifact.setVersion(highestVersion.toString());
    }

    private VersionRangeResult resolveVersionRange(VersionRangeRequest request)
    throws VersionRangeResolutionException {
        return repositorySystem().resolveVersionRange(repositorySystemSession(),
                request);
    }

    private ArtifactResult resolveArtifact(ArtifactRequest request)
    throws ArtifactResolutionException {
        return repositorySystem().resolveArtifact(repositorySystemSession(),
                request);
    }

    private static RepositoryException addSuppressed(
            final RepositoryException head,
            final List<Exception> exceptions) {
        for (Exception ex : exceptions) head.addSuppressed(ex);
        return head;
    }

    private VersionRangeRequest versionRangeRequest(Artifact artifact) {
        return new VersionRangeRequest()
                .setRepositories(remotes)
                .setArtifact(artifact);
    }

    private ArtifactRequest artifactRequest(Artifact artifact) {
        return new ArtifactRequest()
                .setRepositories(remotes)
                .setArtifact(artifact);
    }

    private RepositorySystemSession repositorySystemSession() {
        final RepositorySystemSession rss = this.repositorySystemSession;
        return null != rss
                ? rss
                : (this.repositorySystemSession = newRepositorySystemSession());
    }

    private RepositorySystemSession newRepositorySystemSession() {
        final LoggerFactory loggerFactory = loggerFactory();
        final DefaultRepositorySystemSession session =
                MavenRepositorySystemUtils
                .newSession()
                .setTransferListener(new LogTransferListener(loggerFactory))
                .setRepositoryListener(new LogRepositoryListener(loggerFactory));
        session.setLocalRepositoryManager(
                repositorySystem().newLocalRepositoryManager(session, local));
        return session;
    }

    private RepositorySystem repositorySystem() {
        return serviceLocator().getService(RepositorySystem.class);
    }

    private LoggerFactory loggerFactory() {
        return serviceLocator().getService(LoggerFactory.class);
    }

    private ServiceLocator serviceLocator() {
        final ServiceLocator sl = this.serviceLocator;
        return null != sl ? sl : (this.serviceLocator = newServiceLocator());
    }

    private ServiceLocator newServiceLocator() {
        final DefaultServiceLocator sl = MavenRepositorySystemUtils
                .newServiceLocator()
                .addService(RepositoryConnectorFactory.class,
                        FileRepositoryConnectorFactory.class)
                .addService(RepositoryConnectorFactory.class,
                        WagonRepositoryConnectorFactory.class)
                .setServices(WagonProvider.class, new ManualWagonProvider());
        sl.setErrorHandler(errorHandler());
        return sl;
    }

    private DefaultServiceLocator.ErrorHandler errorHandler() {
        return new DefaultServiceLocator.ErrorHandler() {
            @Override public void serviceCreationFailed(
                    Class<?> type,
                    Class<?> impl,
                    Throwable exception) {
                throw new UndeclaredThrowableException(exception);
            }
        };
    }
}

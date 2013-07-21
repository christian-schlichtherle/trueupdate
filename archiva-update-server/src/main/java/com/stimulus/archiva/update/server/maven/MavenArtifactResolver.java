/*
 * Copyright (C) 2005-2013 Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.server.maven;

import com.stimulus.archiva.update.server.resolver.ArtifactDescriptor;
import com.stimulus.archiva.update.server.resolver.ArtifactResolver;

import java.io.File;
import java.lang.reflect.UndeclaredThrowableException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.annotation.concurrent.ThreadSafe;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositoryException;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.connector.file.FileRepositoryConnectorFactory;
import org.eclipse.aether.connector.wagon.WagonProvider;
import org.eclipse.aether.connector.wagon.WagonRepositoryConnectorFactory;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.VersionRangeRequest;
import org.eclipse.aether.resolution.VersionRangeResolutionException;
import org.eclipse.aether.resolution.VersionRangeResult;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.locator.ServiceLocator;
import org.eclipse.aether.spi.log.LoggerFactory;
import org.eclipse.aether.version.Version;
import static com.stimulus.archiva.update.server.maven.ArtifactConversion.*;

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
        this.remotes = Collections.unmodifiableList(Arrays.asList(remotes));
    }

    @Override public Path resolveArtifactPath(ArtifactDescriptor descriptor)
    throws Exception {
        return resolveArtifactFile(descriptor).toPath();
    }

    private File resolveArtifactFile(ArtifactDescriptor descriptor)
    throws Exception {
        return resolveArtifact(descriptor).getFile();
    }

    private Artifact resolveArtifact(ArtifactDescriptor descriptor)
    throws Exception {
        return resolveArtifact(artifact(descriptor));
    }

    private Artifact resolveArtifact(final Artifact artifact)
    throws RepositoryException {
        return resolveArtifact(artifactRequest(artifact)).getArtifact();
    }

    @Override
    public ArtifactDescriptor resolveUpdateDescriptor(ArtifactDescriptor descriptor)
    throws Exception {
        return descriptor(resolveUpdateArtifact(descriptor));
    }

    private Artifact resolveUpdateArtifact(ArtifactDescriptor descriptor)
    throws Exception {
        return resolveUpdateArtifact(updateRangeArtifact(descriptor));
    }

    private Artifact resolveUpdateArtifact(final Artifact intervalArtifact)
    throws RepositoryException {
        final VersionRangeResult versionRangeResult = resolveVersionRange(
                versionRangeRequest(intervalArtifact));
        final Version highestVersion = versionRangeResult.getHighestVersion();
        return intervalArtifact.setVersion(highestVersion.toString());
    }

    private VersionRangeResult resolveVersionRange(VersionRangeRequest request)
    throws VersionRangeResolutionException {
        return repositorySystem().resolveVersionRange(repositorySystemSession(), request);
    }

    private ArtifactResult resolveArtifact(ArtifactRequest request)
    throws ArtifactResolutionException {
        return repositorySystem().resolveArtifact(repositorySystemSession(), request);
    }

    private VersionRangeRequest versionRangeRequest(final Artifact artifact) {
        return new VersionRangeRequest()
                .setRepositories(remotes)
                .setArtifact(artifact);
    }

    private ArtifactRequest artifactRequest(final Artifact artifact) {
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
        final DefaultRepositorySystemSession session = MavenRepositorySystemUtils
                .newSession()
                .setTransferListener(new LogTransferListener(loggerFactory))
                .setRepositoryListener(new LogRepositoryListener(loggerFactory));
        session.setLocalRepositoryManager(repositorySystem().newLocalRepositoryManager(session, local));
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
                .addService(RepositoryConnectorFactory.class, FileRepositoryConnectorFactory.class)
                .addService(RepositoryConnectorFactory.class, WagonRepositoryConnectorFactory.class)
                .setServices(WagonProvider.class, new ManualWagonProvider());
        sl.setErrorHandler(errorHandler());
        return sl;
    }

    private DefaultServiceLocator.ErrorHandler errorHandler() {
        return new DefaultServiceLocator.ErrorHandler() {
            @Override public void serviceCreationFailed(Class<?> type, Class<?> impl, Throwable exception) {
                throw new UndeclaredThrowableException(exception);
            }
        };
    }
}

/*
 * Copyright (C) 2005-2013 Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.server.maven;

import com.stimulus.archiva.update.server.finder.ArtifactDescriptor;
import com.stimulus.archiva.update.server.finder.UpToDateException;
import com.stimulus.archiva.update.server.finder.UpdateFinder;
import java.io.File;
import java.lang.reflect.UndeclaredThrowableException;
import java.nio.file.Path;
import java.util.Objects;
import javax.annotation.concurrent.Immutable;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositoryException;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
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
import org.eclipse.aether.version.Version;

/**
 * Finds the latest version of a given artifact by looking it up in the local
 * Maven repository.
 *
 * @author Christian Schlichtherle
 */
@Immutable
public class MavenizedUpdateFinder implements UpdateFinder {

    private final LocalRepository localRepository;

    /**
     * Constructs a mavenized update finder which uses the given local
     * repository for artifacts.
     *
     * @param localRepository the local repository for artifacts.
     */
    public MavenizedUpdateFinder(final LocalRepository localRepository) {
        this.localRepository = Objects.requireNonNull(localRepository);
    }

    public static void main(final String[] args) throws Exception {
        System.out.println("Resolved: " + new MavenizedUpdateFinder(new LocalRepository("/Users/christian/.m2/repository")).findUpdatePath(descriptor()));
    }

    private static ArtifactDescriptor descriptor() {
        return new ArtifactDescriptor.Builder()
                .groupId("net.java.truevfs")
                .artifactId("truevfs-profile-full")
                .classifier("shaded")
                .packaging("jar")
                .version("0")
                .build();
    }

    @Override public Path findUpdatePath(ArtifactDescriptor descriptor)
    throws Exception {
        return findUpdateFile(descriptor).toPath();
    }

    private File findUpdateFile(ArtifactDescriptor descriptor)
    throws Exception {
        return findUpdateArtifact(descriptor).getFile();
    }

    private Artifact findUpdateArtifact(ArtifactDescriptor descriptor)
    throws Exception {
        final Artifact artifact = resolve(rangedArtifact(descriptor));
        if (artifact.getVersion().equals(descriptor.version()))
            throw new UpToDateException(descriptor);
        return artifact;
    }

    private Artifact rangedArtifact(ArtifactDescriptor descriptor) {
        return new DefaultArtifact(
                descriptor.groupId(),
                descriptor.artifactId(),
                descriptor.classifier(),
                descriptor.packaging(),
                String.format("[%s,)", descriptor.version()));
    }

    private Artifact resolve(final Artifact rangedArtifact)
    throws RepositoryException {
        final VersionRangeResult versionRangeResult = resolveVersionRange(versionRangeRequest(rangedArtifact));
        final Version version = versionRangeResult.getHighestVersion();
        final Artifact resolvedArtifact = rangedArtifact.setVersion(version.toString());
        final ArtifactResult artifactResult = resolveArtifact(artifactRequest(resolvedArtifact));
        return artifactResult.getArtifact();
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
        final VersionRangeRequest versionRangeRequest = new VersionRangeRequest();
        versionRangeRequest.addRepository(centralRepository());
        versionRangeRequest.setArtifact(artifact);
        return versionRangeRequest;
    }

    private ArtifactRequest artifactRequest(final Artifact artifact) {
        final ArtifactRequest artifactRequest = new ArtifactRequest();
        artifactRequest.addRepository(centralRepository());
        artifactRequest.setArtifact(artifact);
        return artifactRequest;
    }

    private DefaultRepositorySystemSession repositorySystemSession() {
        final DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();
        session.setTransferListener(new ConsoleTransferListener());
        session.setRepositoryListener(new ConsoleRepositoryListener());
        session.setLocalRepositoryManager(repositorySystem().newLocalRepositoryManager(session, localRepository));
        return session;
    }

    private RepositorySystem repositorySystem() {
        final DefaultServiceLocator locator = MavenRepositorySystemUtils.newServiceLocator();
        locator.setErrorHandler(new DefaultServiceLocator.ErrorHandler() {
            @Override public void serviceCreationFailed(Class<?> type, Class<?> impl, Throwable exception) {
                throw new UndeclaredThrowableException(exception);
            }
        });
        locator.addService(RepositoryConnectorFactory.class, FileRepositoryConnectorFactory.class);
        locator.addService(RepositoryConnectorFactory.class, WagonRepositoryConnectorFactory.class);
        locator.setServices(WagonProvider.class, new ManualWagonProvider());
        return locator.getService(RepositorySystem.class);
    }

    private RemoteRepository centralRepository() {
        return new RemoteRepository.Builder("central", "default", "http://repo1.maven.org/maven2/").build();
    }
}

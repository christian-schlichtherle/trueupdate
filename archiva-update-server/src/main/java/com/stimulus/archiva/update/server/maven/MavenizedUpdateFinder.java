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

    public static void main(final String[] args) throws Exception {
        System.out.println("Resolved: " + new MavenizedUpdateFinder().findUpdatePath(descriptor()));
    }

    private static ArtifactDescriptor descriptor() {
        return new ArtifactDescriptor.Builder()
                .groupId("net.java.truevfs")
                .artifactId("truevfs-profile-full")
                .classifier("shaded")
                .packaging("jar")
                .version("0.10.2")
                .build();
    }

    @Override public Path findUpdatePath(ArtifactDescriptor descriptor)
    throws Exception {
        return findUpdateFile(descriptor).toPath();
    }

    private static File findUpdateFile(ArtifactDescriptor descriptor)
    throws Exception {
        return findUpdateArtifact(descriptor).getFile();
    }

    private static Artifact findUpdateArtifact(ArtifactDescriptor descriptor)
    throws Exception {
        final Artifact artifact = resolve(rangedArtifact(descriptor));
        if (artifact.getVersion().equals(descriptor.version()))
            throw new UpToDateException(descriptor);
        return artifact;
    }

    private static Artifact rangedArtifact(ArtifactDescriptor descriptor) {
        return new DefaultArtifact(
                descriptor.groupId(),
                descriptor.artifactId(),
                descriptor.classifier(),
                descriptor.packaging(),
                String.format("[%s,)", descriptor.version()));
    }

    private static Artifact resolve(final Artifact rangedArtifact)
    throws RepositoryException {
        final VersionRangeResult versionRangeResult = resolveVersionRange(versionRangeRequest(rangedArtifact));
        final Version version = versionRangeResult.getHighestVersion();
        final Artifact resolvedArtifact = rangedArtifact.setVersion(version.toString());
        final ArtifactResult artifactResult = resolveArtifact(artifactRequest(resolvedArtifact));
        return artifactResult.getArtifact();
    }

    private static VersionRangeResult resolveVersionRange(VersionRangeRequest request)
    throws VersionRangeResolutionException {
        return repositorySystem().resolveVersionRange(repositorySystemSession(), request);
    }

    private static ArtifactResult resolveArtifact(ArtifactRequest request)
    throws ArtifactResolutionException {
        return repositorySystem().resolveArtifact(repositorySystemSession(), request);
    }

    private static VersionRangeRequest versionRangeRequest(final Artifact artifact) {
        final VersionRangeRequest versionRangeRequest = new VersionRangeRequest();
        versionRangeRequest.addRepository(centralRepository());
        versionRangeRequest.setArtifact(artifact);
        return versionRangeRequest;
    }

    private static ArtifactRequest artifactRequest(final Artifact artifact) {
        final ArtifactRequest artifactRequest = new ArtifactRequest();
        artifactRequest.addRepository(centralRepository());
        artifactRequest.setArtifact(artifact);
        return artifactRequest;
    }

    private static DefaultRepositorySystemSession repositorySystemSession() {
        final DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();

        final LocalRepository localRepo = new LocalRepository("target/local-repo");
        session.setLocalRepositoryManager(repositorySystem().newLocalRepositoryManager(session, localRepo));

        session.setTransferListener(new ConsoleTransferListener());
        session.setRepositoryListener(new ConsoleRepositoryListener());

        return session;
    }

    private static RepositorySystem repositorySystem() {
        // Aether's components implement org.eclipse.aether.spi.locator.ServiceLocator
        // to ease manual wiring and using the prepopulated
        // DefaultServiceLocator, we only need to register the repository
        // connector factories.
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

    private static RemoteRepository centralRepository() {
        return new RemoteRepository.Builder("central", "default", "http://repo1.maven.org/maven2/").build();
    }
}

/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.artifact.impl.maven;

import java.io.File;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.URL;
import java.util.*;
import static java.util.Arrays.asList;
import javax.annotation.*;
import javax.annotation.concurrent.Immutable;
import javax.xml.bind.*;
import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.*;
import static net.java.trueupdate.artifact.impl.maven.ArtifactConverters.*;
import net.java.trueupdate.artifact.spec.ArtifactDescriptor;
import net.java.trueupdate.artifact.spec.ArtifactResolver;
import net.java.trueupdate.core.codec.JaxbCodec;
import net.java.trueupdate.core.io.Source;
import static net.java.trueupdate.shed.Objects.*;
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
 * them up in a local repository and some optional remote repositories.
 *
 * @author Christian Schlichtherle
 */
@Immutable
@XmlRootElement(name = "repositories")
@XmlAccessorType(XmlAccessType.FIELD)
public final class MavenArtifactResolver implements ArtifactResolver {

    @XmlJavaTypeAdapter(LocalRepositoryAdapter.class)
    private final LocalRepository local;

    @XmlElement(name = "remote")
    @XmlJavaTypeAdapter(RemoteRepositoryAdapter.class)
    private final List<RemoteRepository> remotes;

    private transient volatile ServiceLocator serviceLocator;
    private transient volatile RepositorySystemSession repositorySystemSession;

    /** Required for JAXB. */
    private MavenArtifactResolver() {
        local = null;
        remotes = null;
    }

    /**
     * Constructs a Maven artifact resolver which uses the given local and
     * remote repositories.
     *
     * @param local the local repository.
     * @param remotes the array of remote repositories.
     */
    public MavenArtifactResolver(LocalRepository local,
                                 RemoteRepository... remotes) {
        this(local, asList(remotes));
    }

    /**
     * Constructs a Maven artifact resolver which uses the given local and
     * remote repositories.
     *
     * @param local the local repository.
     * @param remotes the list of remote repositories.
     */
    public MavenArtifactResolver(final LocalRepository local,
                                 final List<RemoteRepository> remotes) {
        this.local = requireNonNull(local);
        this.remotes = Collections.unmodifiableList(
                new ArrayList<RemoteRepository>(remotes));
    }

    /** Returns the local repository. */
    public LocalRepository localRepository() { return local; }

    /**
     * Returns a list of remote repositories.
     * The returned list may be unmodifiable.
     * If it is modifiable, then changing it has no effect on this class.
     */
    @SuppressWarnings("ReturnOfCollectionOrArrayField")
    public List<RemoteRepository> remoteRepositories() { return remotes; }

    @Override public File resolveArtifactFile(ArtifactDescriptor descriptor)
    throws Exception {
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
            throw new ArtifactResolutionException(asList(artifactResult),
                    "Artifact not found.");
        return resolved;
    }

    @Override public String resolveUpdateVersion(ArtifactDescriptor descriptor)
    throws Exception {
        return resolveUpdateArtifact(descriptor).getVersion();
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
        if (null == highestVersion)
            throw new VersionRangeResolutionException(result,
                    "Update artifact not found.");
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

    private VersionRangeRequest versionRangeRequest(Artifact artifact) {
        return new VersionRangeRequest()
                .setRepositories(remoteRepositories())
                .setArtifact(artifact);
    }

    private ArtifactRequest artifactRequest(Artifact artifact) {
        return new ArtifactRequest()
                .setRepositories(remoteRepositories())
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
                repositorySystem().newLocalRepositoryManager(session, localRepository()));
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

    private static ServiceLocator newServiceLocator() {
        final DefaultServiceLocator sl = MavenRepositorySystemUtils
                .newServiceLocator()
                .addService(RepositoryConnectorFactory.class,
                        FileRepositoryConnectorFactory.class)
                .addService(RepositoryConnectorFactory.class,
                        WagonRepositoryConnectorFactory.class)
                .setServices(WagonProvider.class, new AhcWagonProvider());
        sl.setErrorHandler(errorHandler());
        if (LegacySlf4jLoggerFactory.AVAILABLE)
            sl.setService(LoggerFactory.class, LegacySlf4jLoggerFactory.class);
        return sl;
    }

    private static DefaultServiceLocator.ErrorHandler errorHandler() {
        return new DefaultServiceLocator.ErrorHandler() {
            @Override public void serviceCreationFailed(
                    Class<?> type,
                    Class<?> impl,
                    Throwable exception) {
                throw new UndeclaredThrowableException(exception);
            }
        };
    }

    @Override public boolean equals(final Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof MavenArtifactResolver)) return false;
        final MavenArtifactResolver that = (MavenArtifactResolver) obj;
        return  this.localRepository().equals(that.localRepository()) &&
                this.remoteRepositories().equals(that.remoteRepositories());
    }

    @Override public int hashCode() {
        int hash = 17;
        hash = 31 * hash + localRepository().hashCode();
        hash = 31 * hash + remoteRepositories().hashCode();
        return hash;
    }

    /** Returns the URL of the {@code "main-repositories.xml"} resource. */
    public static URL mainRepositoriesResource() {
        return MavenArtifactResolver.class.getResource("main-repositories.xml");
    }

    /**
     * Decodes a maven artifact resolver from XML.
     *
     * @param source the source for reading the XML.
     * @return the decoded repositories.
     * @throws Exception at the discretion of the JAXB codec, e.g. if the
     *         source isn't readable.
     */
    public static MavenArtifactResolver decodeFromXml(Source source)
    throws Exception {
        return new JaxbCodec(jaxbContext())
                .decode(source, MavenArtifactResolver.class);
    }

    /** Returns a JAXB context which binds only this class. */
    public static JAXBContext jaxbContext() { return Lazy.JAXB_CONTEXT; }

    private static class Lazy {

        static final JAXBContext JAXB_CONTEXT;

        static {
            try {
                JAXB_CONTEXT = JAXBContext.newInstance(MavenArtifactResolver.class);
            } catch (JAXBException ex) {
                throw new AssertionError(ex);
            }
        }
    }
}

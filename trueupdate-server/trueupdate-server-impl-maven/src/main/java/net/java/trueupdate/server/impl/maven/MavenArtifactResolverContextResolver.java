package net.java.trueupdate.server.impl.maven;

import java.lang.reflect.UndeclaredThrowableException;
import java.net.*;
import javax.annotation.Nullable;
import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.*;
import net.java.trueupdate.artifact.impl.maven.MavenArtifactResolver;
import net.java.trueupdate.artifact.spec.ArtifactResolver;
import net.java.trueupdate.core.io.*;
import net.java.trueupdate.shed.*;

/**
 * A context resolver for a maven artifact resolver.
 *
 * @author Christian Schlichtherle
 */
@Provider
public final class MavenArtifactResolverContextResolver
implements ContextResolver<ArtifactResolver> {

    private @Nullable URI configuration;

    private volatile ArtifactResolver artifactResolver;

    /**
     * Constructs a maven artifact resolver context resolver with the given
     * configuration URI.
     */
    public MavenArtifactResolverContextResolver(final URI configuration) {
        this.configuration = Objects.requireNonNull(configuration);
    }

    /**
     * Constructs a maven artifact resolver context resolver.
     * Use of this constructor requires calling {@link #setServletContext}
     * before use.
     */
    public MavenArtifactResolverContextResolver() { }

    /**
     * Calling this method is required when using the no-arg constructor.
     * This method looks up the configuration URI from the servlet context init
     * parameter with the name "configuration".
     */
    @Context
    public void setServletContext(final ServletContext servletContext)
    throws URISyntaxException {
        this.configuration = new URI(SystemProperties.resolve(
                servletContext.getInitParameter("configuration")));
        artifactResolver = null;
    }

    @Override public ArtifactResolver getContext(final Class<?> type) {
        final ArtifactResolver ar = this.artifactResolver;
        return null != ar
                ? ar
                : (this.artifactResolver = loadArtifactResolver());
    }

    private ArtifactResolver loadArtifactResolver() {
        try { return MavenArtifactResolver.decodeFromXml(source()); }
        catch (RuntimeException ex) { throw ex; }
        catch (Exception ex) { throw new UndeclaredThrowableException(ex); }
    }

    private Source source() throws Exception {
        final URI uri = configuration;
        return uri.isAbsolute()
                ? Sources.forUrl(uri.toURL())
                : Sources.forResource(removeLeadingSlashes(uri.getPath()),
                                      contextClassLoader());
    }

    private static String removeLeadingSlashes(String string) {
        while (string.startsWith("/")) string = string.substring(1);
        return string;
    }

    private static ClassLoader contextClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }
}

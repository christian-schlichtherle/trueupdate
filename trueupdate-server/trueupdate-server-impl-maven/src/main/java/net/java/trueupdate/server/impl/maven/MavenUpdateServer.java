/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.server.impl.maven;

import java.lang.reflect.UndeclaredThrowableException;
import java.net.URI;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import javax.servlet.ServletContext;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import net.java.trueupdate.artifact.impl.maven.MavenArtifactResolver;
import net.java.trueupdate.artifact.spec.ArtifactResolver;
import net.java.trueupdate.core.io.Source;
import net.java.trueupdate.core.io.Sources;
import net.java.trueupdate.jax.rs.server.BasicUpdateServer;
import net.java.trueupdate.shed.SystemProperties;

/**
 * A an artifact update server which uses a maven artifact resolver.
 * For best performance, this should be used like a singleton
 * - see {@link MavenUpdateServerApplication}.
 * Once {@linkplain #setServletContext initialized}, it is safe to use this
 * class in multiple threads.
 *
 * @author Christian Schlichtherle
 */
@Path("/")
@ThreadSafe
public final class MavenUpdateServer extends BasicUpdateServer {

    private volatile @Nullable URI configuration;

    private volatile @Nullable ArtifactResolver artifactResolver;

    /**
     * Constructs a maven update server.
     * Use of this constructor requires calling {@link #setServletContext}
     * before use.
     */
    public MavenUpdateServer() { }

    /**
     * Calling this method is required when using the no-arg constructor.
     * This method looks up the configuration URI from the servlet context
     * init-parameter with the name "configuration".
     * As a side effect, the {@linkplain #artifactResolver() artifact resolver}
     * gets reloaded upon the next call.
     *
     * @throws NullPointerException if {@code context} is {@code null} or has
     *         no setServletContext-parameter named "configuration".
     * @throws IllegalArgumentException if the parameter value is not a valid
     *         URI.
     */
    @Context
    public void setServletContext(final ServletContext context) {
        this.configuration = URI.create(SystemProperties.resolve(
                context.getInitParameter("configuration")));
        artifactResolver = null;
    }

    @Override protected ArtifactResolver artifactResolver() {
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

/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.server.maven;

import java.lang.reflect.UndeclaredThrowableException;
import java.net.*;
import javax.annotation.Resource;
import javax.ejb.*;
import javax.inject.Provider;
import net.java.trueupdate.artifact.maven.MavenArtifactResolver;
import net.java.trueupdate.artifact.api.ArtifactResolver;
import net.java.trueupdate.core.io.*;
import net.java.trueupdate.core.util.SystemProperties;

/**
 * A provider for an artifact resolver which uses Maven.
 *
 * @author Christian Schlichtherle
 */
@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class MavenArtifactResolverProviderBean
implements Provider<ArtifactResolver> {

    // See https://java.net/jira/browse/GLASSFISH-20766 .
    @Resource(name = "configurationUri")
    private String configurationString =
            MavenArtifactResolver.mainRepositoriesResource().toString();

    @Override public ArtifactResolver get() {
        final ArtifactResolver ar = artifactResolver;
        return null != ar ? ar : (artifactResolver = loadArtifactResolver());
    }

    private volatile ArtifactResolver artifactResolver;

    private synchronized ArtifactResolver loadArtifactResolver() {
        if (null != artifactResolver) // DCL w/ volatile is OK
            return artifactResolver;
        try { return MavenArtifactResolver.decodeFromXml(source()); }
        catch (RuntimeException ex) { throw ex; }
        catch (Exception ex) { throw new UndeclaredThrowableException(ex); }
    }

    private Source source() throws Exception {
        final URI uri = configurationUri();
        return uri.isAbsolute()
                ? Sources.forUrl(uri.toURL())
                : Sources.forResource(removeLeadingSlashes(uri.getPath()),
                                      contextClassLoader());
    }

    private URI configurationUri() throws URISyntaxException {
        return new URI(SystemProperties.resolve(configurationString));
    }

    private static String removeLeadingSlashes(String string) {
        while (string.startsWith("/")) string = string.substring(1);
        return string;
    }

    private static ClassLoader contextClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }
}

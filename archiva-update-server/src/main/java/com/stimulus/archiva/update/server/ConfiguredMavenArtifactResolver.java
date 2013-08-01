/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.server;

import com.stimulus.archiva.update.maven.MavenArtifactResolver;
import org.eclipse.aether.repository.LocalRepository;

/**
 * @author Christian Schlichtherle
 */
//@Singleton
public class ConfiguredMavenArtifactResolver extends MavenArtifactResolver {

    public ConfiguredMavenArtifactResolver() {
        super(localRepository());
    }

    static LocalRepository localRepository() {
        return new LocalRepository(
                System.getProperty(propertyKey(), defaultValue()));
    }

    static String propertyKey() {
        return packageName() + ".repository";
    }

    private static String packageName() {
        return ContextResolverForArtifactResolver.class.getPackage().getName();
    }

    private static String defaultValue() {
        return userHome() + "/.m2/repository";
    }

    private static String userHome() { return System.getProperty("user.home"); }
}

/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.server.impl.maven;

import java.util.*;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import net.java.trueupdate.jax.rs.server.ArtifactUpdateServiceExceptionMapper;

/**
 * @author Christian Schlichtherle
 */
@ApplicationPath("/")
public final class ArtifactUpdateServerApplication extends Application {

    private static final Set<Class<?>> classes = Collections.unmodifiableSet(
            new HashSet<>(Arrays.asList(
                ArtifactUpdateServerBean.class,
                ArtifactUpdateServiceExceptionMapper.class)));

    @Override public Set<Class<?>> getClasses() { return classes; }
}

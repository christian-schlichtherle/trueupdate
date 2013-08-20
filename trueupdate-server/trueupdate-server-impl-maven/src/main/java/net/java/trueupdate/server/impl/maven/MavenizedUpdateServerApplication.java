/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.server.impl.maven;

import java.util.*;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import net.java.trueupdate.jax.rs.server.UpdateServiceExceptionMapper;

/**
 * @author Christian Schlichtherle
 */
@ApplicationPath("/")
public final class MavenizedUpdateServerApplication extends Application {

    private static final Set<Class<?>> classes = Collections.unmodifiableSet(
            new HashSet<>(Arrays.asList(
                MavenizedUpdateServerBean.class,
                UpdateServiceExceptionMapper.class)));

    @Override public Set<Class<?>> getClasses() { return classes; }
}

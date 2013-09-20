/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.server.maven;

import java.util.*;
import javax.annotation.concurrent.Immutable;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import net.java.trueupdate.jaxrs.server.UpdateServiceExceptionMapper;

/**
 * An application which provides the class {@link UpdateServiceExceptionMapper}
 * and a new {@link MavenUpdateServer}.
 *
 * @author Christian Schlichtherle
 */
@ApplicationPath("/")
@Immutable
public final class MavenUpdateServerApplication extends Application {

    /** Returns a set with {@link UpdateServiceExceptionMapper}. */
    @SuppressWarnings("unchecked")
    @Override public Set<Class<?>> getClasses() {
        return (Set) Collections.singleton(UpdateServiceExceptionMapper.class);
    }

    /** Returns a set with a new {@link MavenUpdateServer}. */
    @Override public Set<Object> getSingletons() {
        return Collections.singleton((Object) new MavenUpdateServer());
    }
}

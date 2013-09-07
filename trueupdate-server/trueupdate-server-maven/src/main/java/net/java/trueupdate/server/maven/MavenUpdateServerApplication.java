/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.server.maven;

import java.util.*;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import javax.servlet.ServletContext;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import net.java.trueupdate.jaxrs.server.UpdateServiceExceptionMapper;
import net.java.trueupdate.util.Objects;

/**
 * An application which provides the class {@link UpdateServiceExceptionMapper}
 * and a new {@link MavenUpdateServer}.
 * Once {@linkplain #setServletContext initialized}, it is safe to use this
 * class in multiple threads.
 *
 * @author Christian Schlichtherle
 */
@ApplicationPath("/")
@ThreadSafe
public final class MavenUpdateServerApplication extends Application {

    private volatile @Nullable ServletContext context;

    /**
     * Constructs a maven update server application.
     * Use of this constructor requires calling {@link #setServletContext}
     * before use.
     */
    public MavenUpdateServerApplication() { }

    /**
     * Calling this method is required when using the no-arg constructor.
     */
    @Context
    public void setServletContext(final ServletContext context) {
        this.context = Objects.requireNonNull(context);
    }

    /** Returns a set with {@link UpdateServiceExceptionMapper}. */
    @SuppressWarnings("unchecked")
    @Override public Set<Class<?>> getClasses() {
        return (Set) Collections.singleton(UpdateServiceExceptionMapper.class);
    }

    /** Returns a set with a new {@link MavenUpdateServer}. */
    @Override public Set<Object> getSingletons() {
        final MavenUpdateServer mus = new MavenUpdateServer();
        mus.setServletContext(context);
        return Collections.singleton((Object) mus);
    }
}

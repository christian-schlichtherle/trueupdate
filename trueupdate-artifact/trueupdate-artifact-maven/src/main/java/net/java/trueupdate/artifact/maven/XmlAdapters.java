/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.artifact.maven;

import java.io.File;
import javax.annotation.*;
import javax.annotation.concurrent.Immutable;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import static net.java.trueupdate.artifact.maven.XmlAdapters.*;
import net.java.trueupdate.core.util.SystemProperties;
import org.eclipse.aether.repository.*;

@Immutable
final class XmlAdapters {

    private XmlAdapters() { }

    static @Nullable String resolve(@CheckForNull String string) {
        return null == string ? null : SystemProperties.resolve(string);
    }

    static @Nullable String nonEmptyOrNull(String string) {
        return string.isEmpty() ? null : string;
    }
} // XmlAdapters

@Immutable
final class LocalRepositoryAdapter
extends XmlAdapter<Local, LocalRepository> {

    @Override public LocalRepository unmarshal(Local l) {
        return new LocalRepository(new File(resolve(l.basedir)),
                resolve(l.type));
    }

    @Override public Local marshal(final LocalRepository lr) {
        final Local l = new Local();
        l.basedir = nonEmptyOrNull(lr.getBasedir().getPath());
        l.type = nonEmptyOrNull(lr.getContentType());
        return l;
    }
} // LocalRepositoryAdapter

final class Local {
    public String basedir, type;
} // Local

@Immutable
final class RemoteRepositoryAdapter
extends XmlAdapter<Remote, RemoteRepository> {

    @Override public RemoteRepository unmarshal(Remote r) {
        return new RemoteRepository
                .Builder(resolve(r.id), resolve(r.type), resolve(r.url))
                .build();
    }

    @Override public Remote marshal(final RemoteRepository rr) {
        final Remote r = new Remote();
        r.id = nonEmptyOrNull(rr.getId());
        r.type = nonEmptyOrNull(rr.getContentType());
        r.url = nonEmptyOrNull(rr.getUrl());
        return r;
    }
} // RemoteRepositoryAdapter

final class Remote {
    public String id, type, url;
} // Remote

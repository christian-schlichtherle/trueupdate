/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.artifact.maven;

import java.io.File;
import javax.annotation.*;
import javax.annotation.concurrent.Immutable;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import static net.java.trueupdate.artifact.maven.XmlAdapters.*;
import net.java.trueupdate.artifact.maven.ci.*;
import net.java.trueupdate.util.SystemProperties;
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
extends XmlAdapter<LocalRepositoryCi, LocalRepository> {

    @Override
    public LocalRepository unmarshal(LocalRepositoryCi lrd) {
        return new LocalRepository(new File(resolve(lrd.basedir)),
                resolve(lrd.type));
    }

    @Override
    public LocalRepositoryCi marshal(final LocalRepository lr) {
        final LocalRepositoryCi lrd = new LocalRepositoryCi();
        lrd.basedir = nonEmptyOrNull(lr.getBasedir().getPath());
        lrd.type = nonEmptyOrNull(lr.getContentType());
        return lrd;
    }
} // LocalRepositoryAdapter

@Immutable
final class RemoteRepositoryAdapter
extends XmlAdapter<RemoteRepositoryCi, RemoteRepository> {

    @Override
    public RemoteRepository unmarshal(RemoteRepositoryCi rrd) {
        return new RemoteRepository
                .Builder(resolve(rrd.id), resolve(rrd.type), resolve(rrd.url))
                .build();
    }

    @Override
    public RemoteRepositoryCi marshal(final RemoteRepository rr) {
        final RemoteRepositoryCi rrd = new RemoteRepositoryCi();
        rrd.id = nonEmptyOrNull(rr.getId());
        rrd.type = nonEmptyOrNull(rr.getContentType());
        rrd.url = nonEmptyOrNull(rr.getUrl());
        return rrd;
    }
} // RemoteRepositoryAdapter

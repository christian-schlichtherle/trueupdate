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
extends XmlAdapter<LocalRepositoryDescriptor, LocalRepository> {

    @Override
    public LocalRepository unmarshal(LocalRepositoryDescriptor lrd) {
        return new LocalRepository(new File(resolve(lrd.basedir)),
                resolve(lrd.type));
    }

    @Override
    public LocalRepositoryDescriptor marshal(final LocalRepository lr) {
        final LocalRepositoryDescriptor lrd = new LocalRepositoryDescriptor();
        lrd.basedir = nonEmptyOrNull(lr.getBasedir().getPath());
        lrd.type = nonEmptyOrNull(lr.getContentType());
        return lrd;
    }
} // LocalRepositoryAdapter

final class LocalRepositoryDescriptor {
    public String basedir, type;
} // LocalRepositoryDescriptor

@Immutable
final class RemoteRepositoryAdapter
extends XmlAdapter<RemoteRepositoryDescriptor, RemoteRepository> {

    @Override
    public RemoteRepository unmarshal(RemoteRepositoryDescriptor rrd) {
        return new RemoteRepository
                .Builder(resolve(rrd.id), resolve(rrd.type), resolve(rrd.url))
                .build();
    }

    @Override
    public RemoteRepositoryDescriptor marshal(final RemoteRepository rr) {
        final RemoteRepositoryDescriptor rrd = new RemoteRepositoryDescriptor();
        rrd.id = nonEmptyOrNull(rr.getId());
        rrd.type = nonEmptyOrNull(rr.getContentType());
        rrd.url = nonEmptyOrNull(rr.getUrl());
        return rrd;
    }
} // RemoteRepositoryAdapter

final class RemoteRepositoryDescriptor {
    public String id, type, url;
} // RemoteRepositoryDescriptor

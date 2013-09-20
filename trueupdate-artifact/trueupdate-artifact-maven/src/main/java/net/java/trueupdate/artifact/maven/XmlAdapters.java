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
import net.java.trueupdate.artifact.maven.dto.*;
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
extends XmlAdapter<LocalRepositoryDto, LocalRepository> {

    @Override
    public LocalRepository unmarshal(LocalRepositoryDto ci) {
        return new LocalRepository(new File(resolve(ci.basedir)),
                resolve(ci.type));
    }

    @Override
    public LocalRepositoryDto marshal(final LocalRepository lr) {
        final LocalRepositoryDto dto = new LocalRepositoryDto();
        dto.basedir = nonEmptyOrNull(lr.getBasedir().getPath());
        dto.type = nonEmptyOrNull(lr.getContentType());
        return dto;
    }
} // LocalRepositoryAdapter

@Immutable
final class RemoteRepositoryAdapter
extends XmlAdapter<RemoteRepositoryDto, RemoteRepository> {

    @Override
    public RemoteRepository unmarshal(RemoteRepositoryDto ci) {
        return new RemoteRepository
                .Builder(resolve(ci.id), resolve(ci.type), resolve(ci.url))
                .build();
    }

    @Override
    public RemoteRepositoryDto marshal(final RemoteRepository rr) {
        final RemoteRepositoryDto dto = new RemoteRepositoryDto();
        dto.id = nonEmptyOrNull(rr.getId());
        dto.type = nonEmptyOrNull(rr.getContentType());
        dto.url = nonEmptyOrNull(rr.getUrl());
        return dto;
    }
} // RemoteRepositoryAdapter

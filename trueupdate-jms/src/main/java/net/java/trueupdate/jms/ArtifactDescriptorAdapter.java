/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.jms;

import javax.annotation.*;
import javax.annotation.concurrent.Immutable;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import net.java.trueupdate.artifact.spec.ArtifactDescriptor;
import static net.java.trueupdate.util.Objects.nonDefaultOrNull;

/**
 * Marshals an artifact descriptor to its DTO and vice versa.
 *
 * @author Christian Schlichtherle
 */
@Immutable
final class ArtifactDescriptorAdapter
extends XmlAdapter<ArtifactDescriptorDto, ArtifactDescriptor> {

    @Override
    public @Nullable ArtifactDescriptor unmarshal(
            final @CheckForNull ArtifactDescriptorDto dto)
    throws Exception {
        if (null == dto) return null;
        return ArtifactDescriptor
                .builder()
                .groupId(dto.groupId)
                .artifactId(dto.artifactId)
                .version(dto.version)
                .classifier(dto.classifier)
                .packaging(dto.packaging)
                .build();
    }

    @Override
    public @Nullable
    ArtifactDescriptorDto marshal(
            final @CheckForNull ArtifactDescriptor ad)
    throws Exception {
        if (null == ad) return null;
        final ArtifactDescriptorDto dto = new ArtifactDescriptorDto();
        dto.groupId = ad.groupId();
        dto.artifactId = ad.artifactId();
        dto.version = ad.version();
        dto.classifier = nonDefaultOrNull(ad.classifier(), "");
        dto.packaging = nonDefaultOrNull(ad.packaging(), "jar");
        return dto;
    }
}

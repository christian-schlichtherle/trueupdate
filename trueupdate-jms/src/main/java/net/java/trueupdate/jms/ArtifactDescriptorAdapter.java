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
extends XmlAdapter<CompactArtifactDescriptorDto, ArtifactDescriptor> {

    @Override
    public @Nullable ArtifactDescriptor unmarshal(
            final @CheckForNull CompactArtifactDescriptorDto cad)
    throws Exception {
        if (null == cad) return null;
        return ArtifactDescriptor
                .builder()
                .groupId(cad.groupId)
                .artifactId(cad.artifactId)
                .version(cad.version)
                .classifier(cad.classifier)
                .extension(cad.extension)
                .build();
    }

    @Override
    public @Nullable CompactArtifactDescriptorDto marshal(
            final @CheckForNull ArtifactDescriptor ad)
    throws Exception {
        if (null == ad) return null;
        final CompactArtifactDescriptorDto cad = new CompactArtifactDescriptorDto();
        cad.groupId = ad.groupId();
        cad.artifactId = ad.artifactId();
        cad.version = ad.version();
        cad.classifier = nonDefaultOrNull(ad.classifier(), "");
        cad.extension = nonDefaultOrNull(ad.extension(), "jar");
        return cad;
    }
}

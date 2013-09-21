/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.message;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import net.java.trueupdate.artifact.spec.ArtifactDescriptor;
import net.java.trueupdate.message.UpdateMessage.Type;
import net.java.trueupdate.message.dto.CompactArtifactDescriptorDto;
import net.java.trueupdate.message.dto.CompactUpdateMessageDto;

/**
 * Marshals an update message to its DTO and vice versa.
 *
 * @author Christian Schlichtherle
 */
@Immutable
public final class UpdateMessageAdapter
extends XmlAdapter<CompactUpdateMessageDto, UpdateMessage> {

    @Override
    public @Nullable UpdateMessage unmarshal(final @CheckForNull CompactUpdateMessageDto cumd) throws Exception {
        if (null == cumd) return null;
        final CompactArtifactDescriptorDto cadd = cumd.ad;
        return UpdateMessage
                .builder()
                .timestamp(cumd.ts)
                .from(cumd.fr)
                .to(cumd.to)
                .type(Type.valueOf(cumd.ty))
                .artifactDescriptor()
                    .groupId(cadd.g)
                    .artifactId(cadd.a)
                    .version(cadd.v)
                    .classifier(cadd.c)
                    .extension(cadd.e)
                    .inject()
                .updateVersion(cumd.uv)
                .currentLocation(cumd.cl)
                .updateLocation(cumd.ul)
                .status(cumd.st)
                .build();
    }

    @Override
    public @Nullable CompactUpdateMessageDto marshal(final @CheckForNull UpdateMessage um) throws Exception {
        if (null == um) return null;
        final ArtifactDescriptor ad = um.artifactDescriptor();
        final CompactArtifactDescriptorDto cadd = new CompactArtifactDescriptorDto();
        cadd.g = ad.groupId();
        cadd.a = ad.artifactId();
        cadd.v = ad.version();
        cadd.c = ad.classifier();
        cadd.e = ad.extension();
        final CompactUpdateMessageDto cumd = new CompactUpdateMessageDto();
        cumd.ts = um.timestamp();
        cumd.fr = um.from();
        cumd.to = um.to();
        cumd.ty = um.type().name();
        cumd.ad = cadd;
        cumd.uv = um.updateVersion();
        cumd.cl = um.currentLocation();
        cumd.ul = um.updateLocation();
        cumd.st = um.status();
        return cumd;
    }
}

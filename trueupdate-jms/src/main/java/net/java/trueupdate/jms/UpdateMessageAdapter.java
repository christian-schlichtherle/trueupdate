/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.jms;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import net.java.trueupdate.artifact.spec.ArtifactDescriptor;
import net.java.trueupdate.message.UpdateMessage;
import net.java.trueupdate.message.UpdateMessage.Type;

/**
 * Marshals an update message to its DTO and vice versa.
 *
 * @author Christian Schlichtherle
 */
@Immutable
final class UpdateMessageAdapter
extends XmlAdapter<CompactUpdateMessageDto, UpdateMessage> {

    @Override
    public @Nullable UpdateMessage unmarshal(final @CheckForNull CompactUpdateMessageDto cumd) throws Exception {
        if (null == cumd) return null;
        final CompactArtifactDescriptorDto cadd = cumd.artifactDescriptor;
        return UpdateMessage
                .builder()
                .timestamp(cumd.timestamp)
                .from(cumd.from)
                .to(cumd.to)
                .type(Type.valueOf(cumd.type))
                .artifactDescriptor()
                    .groupId(cadd.groupId)
                    .artifactId(cadd.artifactId)
                    .version(cadd.version)
                    .classifier(cadd.classifier)
                    .extension(cadd.extension)
                    .inject()
                .updateVersion(cumd.updateVersion)
                .currentLocation(cumd.currentLocation)
                .updateLocation(cumd.updateLocation)
                .statusText(cumd.statusText)
                .statusCode(cumd.statusCode)
                .statusArgs(cumd.statusArgs)
                .build();
    }

    @Override
    public @Nullable CompactUpdateMessageDto marshal(final @CheckForNull UpdateMessage um) throws Exception {
        if (null == um) return null;
        final ArtifactDescriptor ad = um.artifactDescriptor();
        final CompactArtifactDescriptorDto cadd = new CompactArtifactDescriptorDto();
        cadd.groupId = ad.groupId();
        cadd.artifactId = ad.artifactId();
        cadd.version = ad.version();
        cadd.classifier = nonDefaultOrNull(ad.classifier(), "");
        cadd.extension = nonDefaultOrNull(ad.extension(), "jar");
        final CompactUpdateMessageDto cumd = new CompactUpdateMessageDto();
        cumd.timestamp = um.timestamp();
        cumd.from = um.from();
        cumd.to = um.to();
        cumd.type = um.type().name();
        cumd.artifactDescriptor = cadd;
        cumd.updateVersion = nonDefaultOrNull(um.updateVersion(), "");
        cumd.currentLocation = nonDefaultOrNull(um.currentLocation(), "");
        cumd.updateLocation = nonDefaultOrNull(um.updateLocation(), um.currentLocation());
        cumd.statusText = nonDefaultOrNull(um.statusText(), "");
        cumd.statusCode = nonDefaultOrNull(um.statusCode(), "");
        final Object[] args = um.statusArgs();
        cumd.statusArgs = 0 == args.length ? null : args;
        return cumd;
    }

    private static @Nullable String nonDefaultOrNull(String string,
                                                     @Nullable String defaultValue) {
        return string.equals(defaultValue) ? null : string;
    }
}

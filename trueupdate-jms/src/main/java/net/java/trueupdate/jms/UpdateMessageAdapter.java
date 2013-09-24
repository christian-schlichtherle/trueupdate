/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.jms;

import java.util.logging.Level;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import net.java.trueupdate.artifact.spec.ArtifactDescriptor;
import net.java.trueupdate.message.LogMessage;
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
    public @Nullable UpdateMessage unmarshal(
            final @CheckForNull CompactUpdateMessageDto umd)
    throws Exception {
        if (null == umd) return null;

        final CompactArtifactDescriptorDto add = umd.artifactDescriptor;
        final ArtifactDescriptor ad = ArtifactDescriptor
                .builder()
                .groupId(add.groupId)
                .artifactId(add.artifactId)
                .version(add.version)
                .classifier(add.classifier)
                .extension(add.extension)
                .build();

        final CompactLogMessageDto lmd = umd.logMessage;
        final LogMessage lm = null == lmd
                ? null
                : LogMessage
                    .builder()
                    .level(Level.parse(lmd.level))
                    .message(lmd.message)
                    .parameters(lmd.parameters)
                    .build();

        return UpdateMessage
                .builder()
                .timestamp(umd.timestamp)
                .from(umd.from)
                .to(umd.to)
                .type(Type.valueOf(umd.type))
                .artifactDescriptor(ad)
                .updateVersion(umd.updateVersion)
                .currentLocation(umd.currentLocation)
                .updateLocation(umd.updateLocation)
                .logMessage(lm)
                .build();
    }

    @Override
    public @Nullable CompactUpdateMessageDto marshal(
            final @CheckForNull UpdateMessage um)
    throws Exception {
        if (null == um) return null;

        final ArtifactDescriptor ad = um.artifactDescriptor();
        final CompactArtifactDescriptorDto add = new CompactArtifactDescriptorDto();
        add.groupId = ad.groupId();
        add.artifactId = ad.artifactId();
        add.version = ad.version();
        add.classifier = nonDefaultOrNull(ad.classifier(), "");
        add.extension = nonDefaultOrNull(ad.extension(), "jar");

        final LogMessage lm = um.logMessage();
        final CompactLogMessageDto lmd;
        if (null != lm) {
            lmd = new CompactLogMessageDto();
            lmd.level = lm.level().getName();
            lmd.message = lm.message();
            lmd.parameters = lm.parameters();
        } else {
            lmd = null;
        }

        final CompactUpdateMessageDto umd = new CompactUpdateMessageDto();
        umd.timestamp = um.timestamp();
        umd.from = um.from();
        umd.to = um.to();
        umd.type = um.type().name();
        umd.artifactDescriptor = add;
        umd.updateVersion = nonDefaultOrNull(um.updateVersion(), "");
        umd.currentLocation = um.currentLocation();
        umd.updateLocation = nonDefaultOrNull(um.updateLocation(), um.currentLocation());
        umd.logMessage = lmd;
        return umd;
    }

    private static @Nullable String nonDefaultOrNull(String string,
                                                     @Nullable String defaultValue) {
        return string.equals(defaultValue) ? null : string;
    }
}

/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.jms;

import java.util.List;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import net.java.trueupdate.artifact.spec.ArtifactDescriptor;
import net.java.trueupdate.message.LogMessage;
import net.java.trueupdate.message.UpdateMessage;
import net.java.trueupdate.message.UpdateMessage.Type;
import static net.java.trueupdate.util.Objects.nonDefaultOrNull;

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
            final @CheckForNull CompactUpdateMessageDto cum)
    throws Exception {
        if (null == cum) return null;
        final ArtifactDescriptor ad = new ArtifactDescriptorAdapter()
                .unmarshal(cum.artifactDescriptor);
        final List<LogMessage> lms = new LogMessagesAdapter()
                .unmarshal(cum.logMessages);
        return UpdateMessage
                .builder()
                .timestamp(cum.timestamp)
                .from(cum.from)
                .to(cum.to)
                .type(Type.values()[cum.type])
                .artifactDescriptor(ad)
                .updateVersion(cum.updateVersion)
                .currentLocation(cum.currentLocation)
                .updateLocation(cum.updateLocation)
                .logMessages(lms)
                .build();
    }

    @Override
    public @Nullable CompactUpdateMessageDto marshal(
            final @CheckForNull UpdateMessage um)
    throws Exception {
        if (null == um) return null;
        final CompactArtifactDescriptorDto cad = new ArtifactDescriptorAdapter()
                .marshal(um.artifactDescriptor());
        final CompactLogMessageDto[] clms = new LogMessagesAdapter()
                .marshal(um.logMessages());
        final CompactUpdateMessageDto cum = new CompactUpdateMessageDto();
        cum.timestamp = um.timestamp();
        cum.from = um.from();
        cum.to = um.to();
        cum.type = um.type().ordinal();
        cum.artifactDescriptor = cad;
        cum.updateVersion = nonDefaultOrNull(um.updateVersion(), "");
        cum.currentLocation = um.currentLocation();
        cum.updateLocation = nonDefaultOrNull(um.updateLocation(), um.currentLocation());
        cum.logMessages = clms;
        return cum;
    }
}

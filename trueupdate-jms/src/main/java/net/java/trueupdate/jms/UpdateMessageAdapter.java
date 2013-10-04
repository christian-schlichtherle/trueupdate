/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.jms;

import java.util.List;
import java.util.logging.LogRecord;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import net.java.trueupdate.artifact.spec.ArtifactDescriptor;
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
extends XmlAdapter<UpdateMessageDto, UpdateMessage> {

    @Override
    public @Nullable UpdateMessage unmarshal(
            final @CheckForNull UpdateMessageDto dto)
    throws Exception {
        if (null == dto) return null;
        final ArtifactDescriptor ad = new ArtifactDescriptorAdapter()
                .unmarshal(dto.artifactDescriptor);
        final List<LogRecord> lrs = new LogRecordsAdapter()
                .unmarshal(dto.logRecords);
        final UpdateMessage um = UpdateMessage
                .builder()
                .timestamp(dto.timestamp)
                .from(dto.from)
                .to(dto.to)
                .type(Type.values()[dto.type])
                .artifactDescriptor(ad)
                .updateVersion(dto.updateVersion)
                .currentLocation(dto.currentLocation)
                .updateLocation(dto.updateLocation)
                .build();
        um.attachedLogs().addAll(lrs);
        return um;
    }

    @Override
    public @Nullable
    UpdateMessageDto marshal(
            final @CheckForNull UpdateMessage um)
    throws Exception {
        if (null == um) return null;
        final ArtifactDescriptorDto add = new ArtifactDescriptorAdapter()
                .marshal(um.artifactDescriptor());
        final LogRecordDto[] lrds = new LogRecordsAdapter()
                .marshal(um.attachedLogs());
        final UpdateMessageDto dto = new UpdateMessageDto();
        dto.timestamp = um.timestamp();
        dto.from = um.from();
        dto.to = um.to();
        dto.type = um.type().ordinal();
        dto.artifactDescriptor = add;
        dto.updateVersion = nonDefaultOrNull(um.updateVersion(), "");
        dto.currentLocation = um.currentLocation();
        dto.updateLocation = nonDefaultOrNull(um.updateLocation(), um.currentLocation());
        dto.logRecords = lrds;
        return dto;
    }
}

/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.jms;

import javax.annotation.Nullable;
import javax.xml.bind.annotation.*;

/**
 * A Data Transfer Object (DTO) for an
 * {@link net.java.trueupdate.message.UpdateMessage}.
 *
 * @author Christian Schlichtherle
 */
@SuppressWarnings("PackageVisibleField")
@XmlRootElement(name = "a")
@XmlType(propOrder = { })
@XmlAccessorType(XmlAccessType.FIELD)
@Nullable
final class UpdateMessageDto {

    @XmlElement(name = "a")
    long timestamp;

    @XmlElement(name = "b")
    String from;

    @XmlElement(name = "c")
    String to;

    @XmlElement(name = "d")
    int type;

    @XmlElement(name = "e")
    ArtifactDescriptorDto artifactDescriptor;

    @XmlElement(name = "f")
    String updateVersion;

    @XmlElement(name = "g")
    String currentLocation;

    @XmlElement(name = "h")
    String updateLocation;

    @XmlElement(name = "i")
    LogRecordDto[] logRecords;
}

/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.jms;

import javax.xml.bind.annotation.*;

/**
 * Represents an {@link net.java.trueupdate.message.UpdateMessage}.
 *
 * @author Christian Schlichtherle
 */
@SuppressWarnings("PackageVisibleField")
@XmlRootElement(name = "u")
@XmlType(propOrder = { })
@XmlAccessorType(XmlAccessType.FIELD)
final class CompactUpdateMessageDto {

    @XmlElement(name = "d")
    long timestamp;

    @XmlElement(name = "f")
    String from;

    @XmlElement(name = "t")
    String to;

    @XmlElement(name = "s")
    String type;

    @XmlElement(name = "a")
    CompactArtifactDescriptorDto artifactDescriptor;

    @XmlElement(name = "v")
    String updateVersion;

    @XmlElement(name = "c")
    String currentLocation;

    @XmlElement(name = "u")
    String updateLocation;

    @XmlElement(name = "l")
    CompactLogMessageDto logMessage;
}

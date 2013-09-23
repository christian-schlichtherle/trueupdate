/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.jms;

import javax.xml.bind.annotation.*;

/**
 * Represents an update message.
 *
 * @author Christian Schlichtherle
 */
@SuppressWarnings("PackageVisibleField")
@XmlRootElement(name = "m")
@XmlType(propOrder = { })
@XmlAccessorType(XmlAccessType.FIELD)
final class CompactUpdateMessageDto {

    @XmlElement(name = "a")
    long timestamp;

    @XmlElement(name = "b")
    String from;

    @XmlElement(name = "c")
    String to;

    @XmlElement(name = "d")
    String type;

    @XmlElement(name = "e")
    CompactArtifactDescriptorDto artifactDescriptor;

    @XmlElement(name = "f")
    String updateVersion;

    @XmlElement(name = "g")
    String currentLocation;

    @XmlElement(name = "h")
    String updateLocation;

    @XmlElement(name = "i")
    String statusText;

    @XmlElement(name = "j")
    String statusCode;

    @XmlElement(name = "k")
    Object[] statusArgs;
}

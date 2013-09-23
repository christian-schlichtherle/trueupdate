/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.jms;

import javax.xml.bind.annotation.*;

/**
 * Represents an artifact.
 *
 * @author Christian Schlichtherle
 */
@SuppressWarnings("PackageVisibleField")
@XmlType(propOrder = { })
@XmlAccessorType(XmlAccessType.FIELD)
final class CompactArtifactDescriptorDto {

    @XmlElement(name = "g")
    String groupId;

    @XmlElement(name = "a")
    String artifactId;

    @XmlElement(name = "v")
    String version;

    @XmlElement(name = "c")
    String classifier;

    @XmlElement(name = "e")
    String extension;
}

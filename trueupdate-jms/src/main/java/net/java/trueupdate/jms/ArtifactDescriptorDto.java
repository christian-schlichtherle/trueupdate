/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.jms;

import javax.annotation.Nullable;
import javax.xml.bind.annotation.*;

/**
 * A Data Transfer Object (DTO) for an
 * {@link net.java.trueupdate.artifact.spec.ArtifactDescriptor}.
 *
 * @author Christian Schlichtherle
 */
@SuppressWarnings("PackageVisibleField")
@XmlType(propOrder = { })
@XmlAccessorType(XmlAccessType.FIELD)
@Nullable
final class ArtifactDescriptorDto {

    @XmlElement(name = "a")
    String groupId;

    @XmlElement(name = "b")
    String artifactId;

    @XmlElement(name = "c")
    String version;

    @XmlElement(name = "d")
    String classifier;

    @XmlElement(name = "e")
    String packaging;
}

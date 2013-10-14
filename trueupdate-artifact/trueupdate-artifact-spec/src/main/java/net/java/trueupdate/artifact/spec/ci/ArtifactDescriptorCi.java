/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.artifact.spec.ci;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Represents an artifact descriptor.
 *
 * @author Christian Schlichtherle
 */
@XmlType(name = "ArtifactDescriptor", propOrder = { })
@SuppressWarnings("PublicField")
public final class ArtifactDescriptorCi {

    @XmlElement(required = true)
    public String groupId, artifactId, version;

    public String classifier;

    // Note the use of "extension" as the element name for backwards
    // compatibility with TrueUpdate 0.6 and earlier versions.
    @XmlElement(name = "extension", defaultValue = "jar")
    public String packaging;
}

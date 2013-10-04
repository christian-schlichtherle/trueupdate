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

    /** The classifier. */
    public String classifier;

    /** The extension. */
    @XmlElement(defaultValue = "jar")
    public String extension;
}

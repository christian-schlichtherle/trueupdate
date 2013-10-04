/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.agent.core.ci;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import net.java.trueupdate.artifact.spec.ci.ArtifactDescriptorCi;

/**
 * Represents application parameters.
 *
 * @author Christian Schlichtherle
 */
@XmlType(name = "ApplicationParameters", propOrder = { })
@SuppressWarnings("PublicField")
public final class ApplicationParametersCi {

    @XmlElement(required = true)
    public ArtifactDescriptorCi artifact;

    @XmlElement(name = "location", required = true)
    public String currentLocation;

    public String updateLocation;

    @XmlElement(required = true, defaultValue = "net.java.trueupdate.agent.spec.UpdateAgentListener")
    public String listenerClass;
}

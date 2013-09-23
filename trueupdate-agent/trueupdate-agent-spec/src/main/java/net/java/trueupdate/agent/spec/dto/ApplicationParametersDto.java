/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.agent.spec.dto;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import net.java.trueupdate.artifact.spec.dto.ArtifactDescriptorDto;

/**
 * Configures an application.
 *
 * @author Christian Schlichtherle
 */
@XmlType(name = "ApplicationParameters", propOrder = { })
@SuppressWarnings("PublicField")
public final class ApplicationParametersDto {

    @XmlElement(required = true)
    public ArtifactDescriptorDto artifact;

    @XmlElement(name = "location", required = true)
    public String currentLocation;

    public String updateLocation;

    @XmlElement(required = true, defaultValue = "net.java.trueupdate.agent.spec.UpdateAgentListener")
    public String listenerClass;
}

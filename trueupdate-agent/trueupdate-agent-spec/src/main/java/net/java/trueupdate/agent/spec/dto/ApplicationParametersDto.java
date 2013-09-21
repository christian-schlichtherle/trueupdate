/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.agent.spec.dto;

import net.java.trueupdate.artifact.spec.dto.ArtifactDescriptorDto;

/**
 * Configures an application.
 *
 * @author Christian Schlichtherle
 */
@SuppressWarnings("PublicField")
public class ApplicationParametersDto {
    public ArtifactDescriptorDto artifact;
    public String currentLocation, updateLocation, listenerClass;
}

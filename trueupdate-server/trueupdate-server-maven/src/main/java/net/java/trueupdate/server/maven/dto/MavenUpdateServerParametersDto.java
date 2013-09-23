/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.server.maven.dto;

import javax.xml.bind.annotation.*;
import net.java.trueupdate.artifact.maven.dto.MavenParametersDto;

/**
 * Configures an update server.
 *
 * @author Christian Schlichtherle
 */
@XmlRootElement(name = "server")
@XmlType(name = "MavenUpdateServerParameters", propOrder = { })
@SuppressWarnings("PublicField")
public final class MavenUpdateServerParametersDto {

    @XmlAttribute(required = true)
    public String version;

    @XmlElement(required = true)
    public MavenParametersDto repositories;
}

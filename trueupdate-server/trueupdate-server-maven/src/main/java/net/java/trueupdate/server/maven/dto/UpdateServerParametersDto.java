/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.server.maven.dto;

import javax.xml.bind.annotation.XmlRootElement;
import net.java.trueupdate.artifact.maven.dto.MavenParametersDto;

/**
 * Configures an update server.
 *
 * @author Christian Schlichtherle
 */
@XmlRootElement(name = "server")
@SuppressWarnings("PublicField")
public class UpdateServerParametersDto {
    public MavenParametersDto repositories;
}

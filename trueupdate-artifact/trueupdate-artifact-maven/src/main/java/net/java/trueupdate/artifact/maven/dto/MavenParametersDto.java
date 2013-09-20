/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.artifact.maven.dto;

import java.util.List;
import javax.xml.bind.annotation.XmlElement;

/**
 * Configures Maven.
 *
 * @author Christian Schlichtherle
 */
@SuppressWarnings("PublicField")
public class MavenParametersDto {
    public LocalRepositoryDto local;
    public @XmlElement(name = "remote") List<RemoteRepositoryDto> remotes;
}

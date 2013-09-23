/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.artifact.maven.dto;

import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Configures Maven.
 *
 * @author Christian Schlichtherle
 */
@SuppressWarnings("PublicField")
//@XmlRootElement(name = "repositories")
@XmlType(name = "MavenParameters", propOrder = { "local", "remotes" })
public class MavenParametersDto {

    @XmlElement(required = true)
    public LocalRepositoryDto local;

    @XmlElement(name = "remote")
    public List<RemoteRepositoryDto> remotes;
}

/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.artifact.maven.dto;

import javax.xml.bind.annotation.*;

/**
 * Configures Maven.
 *
 * @author Christian Schlichtherle
 */
@SuppressWarnings("PublicField")
//@XmlRootElement(name = "repositories")
@XmlType(name = "MavenParameters", propOrder = { "local", "remotes" })
public final class MavenParametersDto {

    @XmlElement(required = true)
    public LocalRepositoryDto local;

    @XmlElement(name = "remote")
    public RemoteRepositoryDto[] remotes;
}

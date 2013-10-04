/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.server.maven.ci;

import javax.xml.bind.annotation.*;

import net.java.trueupdate.artifact.maven.ci.MavenParametersCi;

/**
 * Represents Maven update server parameters.
 *
 * @author Christian Schlichtherle
 */
@XmlRootElement(name = "server")
@XmlType(name = "MavenUpdateServerParameters", propOrder = { })
@SuppressWarnings("PublicField")
public final class MavenUpdateServerParametersCi {

    @XmlAttribute(required = true)
    public String version;

    @XmlElement(required = true)
    public MavenParametersCi repositories;
}

/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.server.maven.ci;

import javax.xml.bind.annotation.XmlRootElement;
import net.java.trueupdate.artifact.maven.MavenArtifactResolver;

/**
 * Configures an update server.
 *
 * @author Christian Schlichtherle
 */
@XmlRootElement(name = "server")
@SuppressWarnings("PublicField")
public class UpdateServerCi {
    public MavenArtifactResolver repositories;
}

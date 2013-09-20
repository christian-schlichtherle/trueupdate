/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.artifact.maven.ci;

import java.util.List;
import javax.xml.bind.annotation.XmlElement;

/**
 * Configures Aether.
 *
 * @author Christian Schlichtherle
 */
@SuppressWarnings("PublicField")
public class MavenCi {
    public LocalRepositoryCi local;
    public @XmlElement(name = "remote") List<RemoteRepositoryCi> remotes;
}

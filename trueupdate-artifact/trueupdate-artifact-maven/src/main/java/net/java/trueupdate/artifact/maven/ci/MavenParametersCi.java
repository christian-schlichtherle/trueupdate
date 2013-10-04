/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.artifact.maven.ci;

import javax.xml.bind.annotation.*;

/**
 * Represents Maven parameters.
 *
 * @author Christian Schlichtherle
 */
@SuppressWarnings("PublicField")
//@XmlRootElement(name = "repositories")
@XmlType(name = "MavenParameters", propOrder = { "local", "remotes" })
public final class MavenParametersCi {

    @XmlElement(required = true)
    public LocalRepositoryCi local;

    @XmlElement(name = "remote")
    public RemoteRepositoryCi[] remotes;
}

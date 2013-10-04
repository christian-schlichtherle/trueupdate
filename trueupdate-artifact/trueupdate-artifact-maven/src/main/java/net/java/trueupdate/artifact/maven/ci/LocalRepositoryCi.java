/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.artifact.maven.ci;

import javax.xml.bind.annotation.*;

/**
 * Represents a local repository.
 *
 * @author Christian Schlichtherle
 */
@XmlType(name = "LocalRepository", propOrder = { })
@SuppressWarnings("PublicField")
public final class LocalRepositoryCi {

    @XmlElement(defaultValue = "${user.home}/.m2")
    public String directory;

    public String type;
}

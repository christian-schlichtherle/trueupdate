/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.artifact.maven.dto;

import javax.xml.bind.annotation.*;

/**
 * Configures a local repository.
 *
 * @author Christian Schlichtherle
 */
@XmlType(name = "LocalRepository", propOrder = { })
@SuppressWarnings("PublicField")
public final class LocalRepositoryDto {

    @XmlElement(defaultValue = "${user.home}/.m2")
    public String directory;

    public String type;
}

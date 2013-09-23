/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.artifact.maven.dto;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Configures a local repository.
 *
 * @author Christian Schlichtherle
 */
@XmlType(name = "LocalRepository", propOrder = { })
@SuppressWarnings("PublicField")
public final class LocalRepositoryDto {

    @XmlElement(required = true)
    public String basedir;

    public String type;
}

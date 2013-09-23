/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.artifact.maven.dto;

import javax.xml.bind.annotation.*;

/**
 * Configures a remote repository.
 *
 * @author Christian Schlichtherle
 */
@XmlType(name = "RemoteRepository", propOrder = { })
@SuppressWarnings("PublicField")
public final class RemoteRepositoryDto {

    @XmlElement(defaultValue = "http://repo1.maven.org/maven2/")
    public String url;

    public String id, type;
}

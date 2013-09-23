/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.artifact.maven.dto;

import javax.xml.bind.annotation.XmlType;

/**
 * Configures a remote repository.
 *
 * @author Christian Schlichtherle
 */
@XmlType(name = "RemoteRepository", propOrder = { })
@SuppressWarnings("PublicField")
public class RemoteRepositoryDto {

    public String id, type, url;
}

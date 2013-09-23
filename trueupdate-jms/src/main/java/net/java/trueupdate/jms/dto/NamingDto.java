/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.jms.dto;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Addresses a JNDI context by an initial context class name and a relative
 * path name.
 *
 * @author Christian Schlichtherle
 */
@XmlType(name = "Naming", propOrder = { })
@SuppressWarnings("PublicField")
public class NamingDto {

    @XmlElement(required = true)
    public String initialContextClass, relativePath;
}

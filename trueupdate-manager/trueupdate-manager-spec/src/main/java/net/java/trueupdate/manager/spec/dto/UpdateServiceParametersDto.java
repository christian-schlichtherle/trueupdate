/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.spec.dto;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Configures an update service.
 *
 * @author Christian Schlichtherle
 */
@XmlType(name = "UpdateServiceParameters", propOrder = { })
@SuppressWarnings("PublicField")
public final class UpdateServiceParametersDto {

    @XmlElement(required = true)
    public String uri;
}

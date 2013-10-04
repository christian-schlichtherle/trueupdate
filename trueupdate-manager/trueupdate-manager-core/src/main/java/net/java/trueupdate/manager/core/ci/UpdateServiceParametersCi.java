/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.core.ci;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Represents update service parameters.
 *
 * @author Christian Schlichtherle
 */
@XmlType(name = "UpdateServiceParameters", propOrder = { })
@SuppressWarnings("PublicField")
public final class UpdateServiceParametersCi {

    @XmlElement(required = true)
    public String uri;
}

/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.jms;

import javax.xml.bind.annotation.*;

/**
 * Represents a {@link net.java.trueupdate.message.LogMessage}.
 *
 * @author Christian Schlichtherle
 */
@SuppressWarnings("PackageVisibleField")
@XmlType(propOrder = { })
@XmlAccessorType(XmlAccessType.FIELD)
final class CompactLogMessageDto {

    @XmlElement(name = "l")
    String level;

    @XmlElement(name = "m")
    String message;

    @XmlElement(name = "p")
    Object[] parameters;
}

/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.jms;

import javax.annotation.Nullable;
import javax.xml.bind.annotation.*;

/**
 * Represents a {@link net.java.trueupdate.message.LogMessage}.
 *
 * @author Christian Schlichtherle
 */
@SuppressWarnings("PackageVisibleField")
@XmlType(propOrder = { })
@XmlAccessorType(XmlAccessType.FIELD)
@Nullable
final class CompactLogMessageDto {

    @XmlElement(name = "l")
    String level;

    @XmlElement(name = "c")
    String code;

    @XmlElement(name = "a")
    Object[] args;
}

/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.jms;

import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * A Data Transfer Object (DTO) for a {@link java.util.logging.LogRecord}.
 *
 * @author Christian Schlichtherle
 */
@XmlType(propOrder = { })
@XmlAccessorType(XmlAccessType.FIELD)
@Nullable
final class LogRecordDto {

    @XmlElement(name = "a")
    String loggerName;

    @XmlElement(name = "b")
    String resourceBundleName;

    @XmlElement(name = "c")
    String level;

    @XmlElement(name = "d")
    long sequenceNumber;

    @XmlElement(name = "e")
    String sourceClassName;

    @XmlElement(name = "f")
    String sourceMethodName;

    @XmlElement(name = "g")
    String message;

    @XmlElement(name = "h")
    Object[] parameters;

    @XmlElement(name = "i")
    int threadId;

    @XmlElement(name = "j")
    long millis;

    @XmlElement(name = "k")
    String thrown;
}

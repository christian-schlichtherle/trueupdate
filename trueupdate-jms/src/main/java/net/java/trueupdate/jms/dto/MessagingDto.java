/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.jms.dto;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Addresses JMS administered objects by their JNDI name.
 *
 * @author Christian Schlichtherle
 */
@XmlType(name = "Messaging", propOrder = { })
@SuppressWarnings("PublicField")
public class MessagingDto {

    public NamingDto naming;

    @XmlElement(required = true)
    public String connectionFactory, from;

    public String to;
}

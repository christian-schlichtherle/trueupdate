/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.jms.ci;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Represents messaging parameters.
 * The strings are used as JNDI names for looking up JMS administered objects.
 *
 * @author Christian Schlichtherle
 */
@XmlType(name = "JmsParameters", propOrder = { })
@SuppressWarnings("PublicField")
public final class JmsParametersCi {

    public JndiParametersCi naming;

    @XmlElement(required = true)
    public String connectionFactory, from;

    public String to;
}

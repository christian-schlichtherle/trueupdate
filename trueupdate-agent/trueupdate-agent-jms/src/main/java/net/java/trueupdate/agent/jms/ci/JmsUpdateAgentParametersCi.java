/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.agent.jms.ci;

import javax.xml.bind.annotation.*;
import net.java.trueupdate.agent.core.ci.*;
import net.java.trueupdate.jms.ci.MessagingParametersCi;

/**
 * Represents JMS update agent parameters.
 *
 * @author Christian Schlichtherle
 */
@XmlRootElement(name = "agent")
@XmlType(name = "JmsUpdateAgentParameters", propOrder = { })
@SuppressWarnings("PublicField")
public final class JmsUpdateAgentParametersCi {

    @XmlAttribute(required = true)
    public String version;

    @XmlElement(required = true)
    public ApplicationParametersCi application;

    public TimerParametersCi subscriptionTimer;

    @XmlElement(required = true)
    public MessagingParametersCi messaging;
}

/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.jms.ci;

import javax.xml.bind.annotation.*;

import net.java.trueupdate.jms.ci.JmsParametersCi;
import net.java.trueupdate.manager.core.ci.*;

/**
 * Represents JMS update manager parameters.
 *
 * @author Christian Schlichtherle
 */
@XmlRootElement(name = "manager")
@XmlType(name = "JmsUpdateManagerParameters", propOrder = { })
@SuppressWarnings("PublicField")
public final class JmsUpdateManagerParametersCi {

    @XmlAttribute(required = true)
    public String version;

    @XmlElement(required = true)
    public UpdateServiceParametersCi updateService;

    @XmlElement(required = true)
    public TimerParametersCi updateTimer;

    @XmlElement(required = true)
    public JmsParametersCi messaging;
}

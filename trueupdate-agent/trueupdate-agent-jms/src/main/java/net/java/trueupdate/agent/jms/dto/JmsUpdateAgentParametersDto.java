/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.agent.jms.dto;

import javax.xml.bind.annotation.*;
import net.java.trueupdate.agent.spec.dto.ApplicationParametersDto;
import net.java.trueupdate.jms.dto.MessagingParametersDto;

/**
 * Configures an update agent.
 *
 * @author Christian Schlichtherle
 */
@XmlRootElement(name = "agent")
@XmlType(name = "JmsUpdateAgentParameters", propOrder = { })
@SuppressWarnings("PublicField")
public final class JmsUpdateAgentParametersDto {

    @XmlAttribute(required = true)
    public String version;

    @XmlElement(required = true)
    public ApplicationParametersDto application;

    @XmlElement(required = true)
    public MessagingParametersDto messaging;
}

/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.agent.jms.dto;

import net.java.trueupdate.jms.dto.MessagingDto;
import net.java.trueupdate.agent.spec.dto.ApplicationParametersDto;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Configures an update agent.
 *
 * @author Christian Schlichtherle
 */
@XmlRootElement(name = "agent")
@SuppressWarnings("PublicField")
public class JmsUpdateAgentParametersDto {
    public ApplicationParametersDto application;
    public MessagingDto messaging;
}

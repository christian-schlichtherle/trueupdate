/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.agent.servlets.dto;

import net.java.trueupdate.jms.ci.NamingDto;
import net.java.trueupdate.jms.ci.MessagingDto;
import net.java.trueupdate.agent.spec.dto.ApplicationParametersDto;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Configures an update agent.
 *
 * @author Christian Schlichtherle
 */
@XmlRootElement(name = "agent")
@SuppressWarnings("PublicField")
public class UpdateAgentParametersDto {
    public ApplicationParametersDto application;
    public NamingDto naming;
    public MessagingDto messaging;
}

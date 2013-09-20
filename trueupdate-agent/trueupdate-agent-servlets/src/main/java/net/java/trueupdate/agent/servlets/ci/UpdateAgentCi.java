/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.agent.servlets.ci;

import net.java.trueupdate.jms.ci.NamingCi;
import net.java.trueupdate.jms.ci.MessagingCi;
import net.java.trueupdate.agent.spec.ci.ApplicationCi;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Configures an update agent.
 *
 * @author Christian Schlichtherle
 */
@XmlRootElement(name = "agent")
@SuppressWarnings("PublicField")
public class UpdateAgentCi {
    public ApplicationCi application;
    public NamingCi naming;
    public MessagingCi messaging;
}

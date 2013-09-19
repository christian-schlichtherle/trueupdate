/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.agent.mini.config;

import javax.xml.bind.annotation.XmlRootElement;
import net.java.trueupdate.jms.config.*;

/**
 * Configures an update agent.
 *
 * @author Christian Schlichtherle
 */
@XmlRootElement(name = "agent")
@SuppressWarnings("PublicField")
public class UpdateAgentConfiguration {
    public ApplicationConfiguration application;
    public NamingConfiguration naming;
    public MessagingConfiguration messaging;
}

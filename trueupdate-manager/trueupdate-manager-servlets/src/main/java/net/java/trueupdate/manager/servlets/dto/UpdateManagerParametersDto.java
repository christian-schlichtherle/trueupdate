/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.servlets.dto;

import net.java.trueupdate.jms.ci.NamingDto;
import net.java.trueupdate.jms.ci.MessagingDto;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Configures an update manager.
 *
 * @author Christian Schlichtherle
 */
@XmlRootElement(name = "manager")
@SuppressWarnings("PublicField")
public class UpdateManagerParametersDto {
    public String updateServiceBaseUri, checkUpdatesIntervalMinutes;
    public NamingDto naming;
    public MessagingDto messaging;
}

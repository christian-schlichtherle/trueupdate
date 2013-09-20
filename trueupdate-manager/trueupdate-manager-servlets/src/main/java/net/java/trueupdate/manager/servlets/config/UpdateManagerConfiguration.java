/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.servlets.config;

import javax.xml.bind.annotation.XmlRootElement;
import net.java.trueupdate.jms.config.*;

/**
 * Configures an update manager.
 *
 * @author Christian Schlichtherle
 */
@XmlRootElement(name = "manager")
@SuppressWarnings("PublicField")
public class UpdateManagerConfiguration {
    public String updateServiceBaseUri, checkUpdatesIntervalMinutes;
    public NamingConfiguration naming;
    public MessagingConfiguration messaging;
}

/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.servlets.ci;

import net.java.trueupdate.jms.ci.NamingCi;
import net.java.trueupdate.jms.ci.MessagingCi;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Configures an update manager.
 *
 * @author Christian Schlichtherle
 */
@XmlRootElement(name = "manager")
@SuppressWarnings("PublicField")
public class UpdateManagerCi {
    public String updateServiceBaseUri, checkUpdatesIntervalMinutes;
    public NamingCi naming;
    public MessagingCi messaging;
}

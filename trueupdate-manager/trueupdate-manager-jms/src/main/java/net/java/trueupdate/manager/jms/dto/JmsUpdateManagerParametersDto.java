/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.jms.dto;

import net.java.trueupdate.jms.dto.MessagingDto;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Configures an update manager.
 *
 * @author Christian Schlichtherle
 */
@XmlRootElement(name = "manager")
@SuppressWarnings("PublicField")
public class JmsUpdateManagerParametersDto {
    public String updateServiceBaseUri, checkUpdatesIntervalMinutes;
    public MessagingDto messaging;
}

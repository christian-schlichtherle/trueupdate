/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.jms.dto;

import javax.xml.bind.annotation.*;
import net.java.trueupdate.jms.dto.MessagingParametersDto;
import net.java.trueupdate.manager.spec.dto.*;

/**
 * Configures an update manager.
 *
 * @author Christian Schlichtherle
 */
@XmlRootElement(name = "manager")
@XmlType(name = "JmsUpdateManagerParameters", propOrder = { })
@SuppressWarnings("PublicField")
public final class JmsUpdateManagerParametersDto {

    @XmlAttribute(required = true)
    public String version;

    @XmlElement(required = true)
    public UpdateServiceParametersDto updateService;

    @XmlElement(required = true)
    public TimerParametersDto checkForUpdates;

    @XmlElement(required = true)
    public MessagingParametersDto messaging;
}

/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.agent.spec.dto;

import javax.xml.bind.annotation.*;

/**
 * Configures a timer.
 *
 * @author Christian Schlichtherle
 */
@XmlType(name = "TimerParameters", propOrder = { })
@SuppressWarnings("PublicField")
public final class TimerParametersDto {

    @XmlElement(defaultValue = "0")
    public String delay;

    public String unit;
}

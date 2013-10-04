/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.core.ci;

import javax.xml.bind.annotation.*;

/**
 * Represents timer parameters.
 *
 * @author Christian Schlichtherle
 */
@XmlType(name = "TimerParameters", propOrder = { })
@SuppressWarnings("PublicField")
public final class TimerParametersCi {

    @XmlElement(defaultValue = "0")
    public String delay, period;

    public String unit;
}

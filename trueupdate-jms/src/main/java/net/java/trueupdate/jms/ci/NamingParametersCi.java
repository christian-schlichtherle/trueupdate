/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.jms.ci;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Represents naming parameters.
 * The strings reference an initial context class and the JNDI name of the
 * context.
 *
 * @author Christian Schlichtherle
 */
@XmlType(name = "NamingParameters", propOrder = { })
@SuppressWarnings("PublicField")
public final class NamingParametersCi {

    @XmlElement(defaultValue = "javax.naming.InitialContext")
    public String initialContextClass;

    public String contextLookup;
}

/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.jms.ci;

/**
 * Addresses JMS administered objects by their JNDI name.
 *
 * @author Christian Schlichtherle
 */
@SuppressWarnings("PublicField")
public class MessagingCi {
    public String connectionFactory, from, to;
}

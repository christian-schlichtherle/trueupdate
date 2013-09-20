/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.jms.ci;

/**
 * Addresses JMS administered objects by their JNDI name.
 *
 * @author Christian Schlichtherle
 */
@SuppressWarnings("PublicField")
public class MessagingDto {
    public String connectionFactory, from, to;
}

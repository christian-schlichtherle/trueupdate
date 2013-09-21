/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.jms.dto;

/**
 * Addresses JMS administered objects by their JNDI name.
 *
 * @author Christian Schlichtherle
 */
@SuppressWarnings("PublicField")
public class MessagingDto {
    public NamingDto naming;
    public String connectionFactory, from, to;
}

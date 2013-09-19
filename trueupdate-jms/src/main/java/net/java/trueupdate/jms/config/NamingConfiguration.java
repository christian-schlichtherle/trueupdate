/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.jms.config;

/**
 * Addresses a JNDI context by an initial context class name and a relative
 * path name.
 *
 * @author Christian Schlichtherle
 */
@SuppressWarnings("PublicField")
public class NamingConfiguration {
    public String initialContextClass, relativePath;
}

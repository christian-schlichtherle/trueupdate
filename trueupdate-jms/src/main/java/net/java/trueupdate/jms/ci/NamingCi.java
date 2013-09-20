/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.jms.ci;

/**
 * Addresses a JNDI context by an initial context class name and a relative
 * path name.
 *
 * @author Christian Schlichtherle
 */
@SuppressWarnings("PublicField")
public class NamingCi {
    public String initialContextClass, relativePath;
}

/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.jms;

import javax.annotation.CheckForNull;

/**
 * A Data Transfer Object (DTO) for JNDI.
 *
 * @see DTO#parameters
 * @author Christian Schlichtherle
 */
public final class NamingDto {
    public @CheckForNull String initialContextClass, lookup;
}

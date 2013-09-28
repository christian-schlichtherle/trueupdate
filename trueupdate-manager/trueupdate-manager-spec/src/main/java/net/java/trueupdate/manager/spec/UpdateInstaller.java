/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.spec;

/**
 * Cooperates with an update resolver to install artifact updates.
 * <p>
 * Applications have no need to implement this class and should not do so
 * because it may be subject to future expansion.
 *
 * @author Christian Schlichtherle
 */
public interface UpdateInstaller {

    /**
     * Updates the application as described by the properties of the given
     * update context.
     *
     * @param context the update context.
     *                The implementation should use this object to log any
     *                progress rather than using the {@code java.util.logging}
     *                API directly.
     */
    void install(UpdateContext context) throws Exception;
}

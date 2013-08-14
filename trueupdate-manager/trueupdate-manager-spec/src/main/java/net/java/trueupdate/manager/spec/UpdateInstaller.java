/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.spec;

/**
 * Installs application updates.
 * <p>
 * Implementations should be immutable and hence, thread-safe.
 * <p>
 * Applications have no need to implement this class and should not do so
 * because it may be subject to future expansion.
 *
 * @author Christian Schlichtherle
 */
public interface UpdateInstaller {

    /**
     * Installs the update as described in the given update message.
     *
     * @param message the update message
     * @throws Exception at the discretion of the implementation.
     */
    void install(UpdateMessage message) throws Exception;
}

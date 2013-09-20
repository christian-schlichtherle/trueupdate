/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.spec;

import java.io.File;

/**
 * Cooperates with an update resolver to install artifact updates.
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
     * Updates the application described in the given update message using the
     * given artifact diff zip file.
     *
     * @param message the update message of the
     *        {@linkplain UpdateMessage#type() type}
     *        {@link UpdateMessage.Type#INSTALLATION_REQUEST}.
     * @param diffZip the diff zip file for the current artifact.
     *        The implementation must not modify or delete this file.
     * @throws Exception at the discretion of the implementation.
     */
    void install(UpdateMessage message, File diffZip) throws Exception;
}

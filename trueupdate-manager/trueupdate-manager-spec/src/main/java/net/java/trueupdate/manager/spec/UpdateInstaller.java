/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.spec;

import java.io.File;
import net.java.trueupdate.message.UpdateMessage;

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
     * Updates the application described in the given update message using the
     * given artifact diff zip file.
     *
     * @param message the update message.
     *        The {@linkplain UpdateMessage#type() type} of the update message
     *        is
     *        {@link net.java.trueupdate.message.UpdateMessage.Type#INSTALLATION_REQUEST}.
     * @param diffZip the diff zip file for the current artifact.
     *        The implementation must not modify or delete this file.
     * @param monitor the progress monitor.
     *        The installer should use this object to log any progress rather
     *        than using the {@code java.util.logging} API directly.
     */
    void install(UpdateMessage message, File diffZip, ProgressMonitor monitor)
    throws Exception;
}

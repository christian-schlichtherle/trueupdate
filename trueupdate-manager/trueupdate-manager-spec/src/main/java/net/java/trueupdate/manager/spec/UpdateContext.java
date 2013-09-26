/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.spec;

import java.io.File;

/**
 * An update context.
 * <p>
 * Implementations must be thread-safe.
 * <p>
 * Applications have no need to implement this class and should not do so
 * because it may be subject to future expansion.
 *
 * @author Christian Schlichtherle
 */
public interface UpdateContext {

    /** Returns the current location. */
    String currentLocation();

    /** Returns the update location. */
    String updateLocation();

    /**
     * Returns the diff zip file for patching the current artifact.
     * The client must not modify or delete this file.
     */
    File diffZip();

    /**
     * Sends a redeployment request to the update agent and waits for a
     * response.
     * This handshake ensures that the update agent has processed all progress
     * notices before the redeployment is happening.
     */
    void prepareUndeployment() throws Exception;

    void performUndeployment() throws Exception;

    void rollbackUndeployment() throws Exception;

    void commitUndeployment() throws Exception;
}

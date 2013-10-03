/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.spec;

import java.io.File;
import net.java.trueupdate.manager.spec.tx.Transaction;

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

    /** Returns the current location of the client application. */
    String currentLocation();

    /** Returns the update location of the client application. */
    String updateLocation();

    /**
     * Returns the delta ZIP file for patching the artifact at the current
     * location of the client application.
     * The client must not modify or delete this file.
     */
    File deltaZip();

    /**
     * Decorates the given transaction with the given identifier for execution.
     */
    Transaction decorate(Action id, Transaction tx);
}

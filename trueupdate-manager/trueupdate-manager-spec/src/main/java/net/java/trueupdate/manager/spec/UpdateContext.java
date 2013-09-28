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

    /** Returns the current location. */
    String currentLocation();

    /** Returns the update location. */
    String updateLocation();

    /**
     * Returns the diff zip file for patching the current artifact.
     * The client must not modify or delete this file.
     */
    File diffZip();

    /** Decorates the given update transaction with the given identifier. */
    Transaction decorate(Action id, Transaction tx);
}

/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.spec.tx;

/**
 * Indicates a failure when {@linkplain Transaction#rollback() rolling back} or
 * {@linkplain Transaction#commit() committing} a transaction.
 *
 * @author Christian Schlichtherle
 */
public class TransactionException extends Exception {

    private static final long serialVersionUID = 0L;

    TransactionException(Throwable cause) { super(cause); }
}

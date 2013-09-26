/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.core;

import java.util.logging.LogRecord;

/**
 * A log channel transmits log records.
 *
 * @author Christian Schlichtherle
 */
interface LogChannel {

    /** Transmits the given log record. */
    void transmit(LogRecord record) throws Exception;
}

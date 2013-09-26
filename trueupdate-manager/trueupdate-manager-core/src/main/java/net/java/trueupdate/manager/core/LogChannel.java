/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.core;

import java.util.logging.LogRecord;

/**
 * A channel for transferring {@link LogRecord}s.
 *
 * @author Christian Schlichtherle
 */
public interface LogChannel {
    void transfer(LogRecord record) throws Exception;
}

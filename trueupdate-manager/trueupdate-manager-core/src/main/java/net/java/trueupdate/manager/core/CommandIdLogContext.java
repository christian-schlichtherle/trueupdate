/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.core;

import net.java.trueupdate.manager.spec.CommandId;
import net.java.trueupdate.manager.spec.cmd.LogContext;

import java.util.logging.Logger;

/**
 * @author Christian Schlichtherle
 */
abstract class CommandIdLogContext extends LogContext {

    abstract String loggerName();
    abstract CommandId commandId();

    @Override final protected Logger logger() {
        return Logger.getLogger(loggerName(), resourceBundleName());
    }

    String resourceBundleName() { return CommandId.resourceBundleName(); }

    @Override final protected String startingMessage(Method method) {
        // The log message can use its parameters to figure the method.
        return commandId().beginKey();
    }

    @Override final protected String succeededMessage(Method method) {
        // The log message can use its parameters to figure the method and status.
        return commandId().endKey();
    }

    @Override final protected String failedMessage(Method method) {
        // The log message can use its parameters to figure the method and status.
        return commandId().endKey();
    }
}

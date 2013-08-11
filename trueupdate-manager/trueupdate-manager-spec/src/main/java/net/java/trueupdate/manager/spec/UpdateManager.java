/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.spec;

import net.java.trueupdate.message.UpdateMessage;

/**
 * @author Christian Schlichtherle
 */
public interface UpdateManager {

    void subscribe(UpdateMessage message) throws UpdateException;
    void install(UpdateMessage message) throws UpdateException;
    void unsubscribe(UpdateMessage message) throws UpdateException;
}

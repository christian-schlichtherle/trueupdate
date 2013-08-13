/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.agent.ejb;

import net.java.trueupdate.agent.spec.UpdateAgent;
import net.java.trueupdate.message.UpdateMessageListener;

/**
 * @author Christian Schlichtherle
 */
public interface UpdateAgentBuilder
extends UpdateMessageListener, UpdateAgent.Builder {
}

/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.agent.impl.javaee;

import net.java.trueupdate.agent.core.UpdateMessageDispatcher;
import javax.ejb.Singleton;
import net.java.trueupdate.agent.core.*;

/**
 * An update agent dispatcher bean.
 *
 * @author Christian Schlichtherle
 */
@Singleton
public class UpdateMessageDispatcherBean
extends BasicUpdateMessageDispatcher
implements UpdateMessageDispatcher { }

/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.agent.impl.javaee;

import javax.annotation.*;
import javax.ejb.*;
import javax.jms.*;
import net.java.trueupdate.agent.core.*;
import net.java.trueupdate.agent.spec.UpdateAgent;

/**
 * An update agent builder bean.
 *
 * @author Christian Schlichtherle
 */
@Stateful
@SuppressWarnings("PackageVisibleField")
public class UpdateAgentBuilderBean
extends BasicUpdateAgentBuilder
implements UpdateAgent.Builder {

    @Resource
    ConnectionFactory connectionFactory;

    @Resource(name = "destination", mappedName = "jms/TrueUpdate Manager")
    Destination destination;

    @EJB
    UpdateMessageDispatcher updateMessageDispatcher;

    @Remove
    @Override public UpdateAgent build() {
        return new ConfiguredUpdateAgent(applicationParameters, this);
    }
}

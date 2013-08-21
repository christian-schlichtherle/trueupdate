/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.agent.impl.javaee;

import javax.annotation.*;
import javax.ejb.*;
import javax.jms.*;
import net.java.trueupdate.agent.spec.UpdateAgent;
import net.java.trueupdate.agent.core.BasicUpdateAgentBuilder;

/**
 * An update agent builder bean.
 *
 * @author Christian Schlichtherle
 */
@Stateful
@SuppressWarnings("PackageVisibleField")
@Local(UpdateAgent.Builder.class)
public class UpdateAgentBuilderBean extends BasicUpdateAgentBuilder {

    @Resource
    ConnectionFactory connectionFactory;

    @Resource(name = "TrueUpdate")
    Topic destination;

    @EJB
    UpdateAgentDispatcherBean updateAgentDispatcher;

    @Remove
    @Override public UpdateAgent build() {
        return new ConfiguredUpdateAgent(applicationParameters, this);
    }
}

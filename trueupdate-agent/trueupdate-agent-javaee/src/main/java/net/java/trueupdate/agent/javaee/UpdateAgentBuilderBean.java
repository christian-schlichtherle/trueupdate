/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.agent.javaee;

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
@DependsOn("UpdateAgentMessageDispatcherBean")
@SuppressWarnings("PackageVisibleField")
public class UpdateAgentBuilderBean
extends BasicUpdateAgentBuilder<UpdateAgentBuilderBean>
implements UpdateAgent.Builder<UpdateAgentBuilderBean> {

    @Resource(name = "connectionFactory")
    ConnectionFactory connectionFactory;

    @Resource(name = "destination", lookup = "jms/TrueUpdate Manager")
    Destination destination;

    @EJB
    UpdateAgentMessageDispatcherBean updateAgentMessageDispatcher;

    @Remove
    @Override public UpdateAgent build() {
        return new ConfiguredUpdateAgent(applicationParameters, this);
    }
}

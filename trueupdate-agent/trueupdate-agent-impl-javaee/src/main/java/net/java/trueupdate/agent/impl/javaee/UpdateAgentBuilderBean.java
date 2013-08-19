/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.agent.impl.javaee;

import javax.annotation.*;
import javax.ejb.*;
import javax.jms.*;
import net.java.trueupdate.agent.impl.core.BasicUpdateAgentBuilder;
import net.java.trueupdate.agent.spec.*;
import net.java.trueupdate.manager.spec.*;

/**
 * An update agent builder bean.
 *
 * @author Christian Schlichtherle
 */
@Singleton
@SuppressWarnings("PackageVisibleField")
@Local({ UpdateMessageListener.class, UpdateAgent.Builder.class })
public class UpdateAgentBuilderBean extends BasicUpdateAgentBuilder {

    @Resource
    ConnectionFactory connectionFactory;

    @Resource(name = "TrueUpdate")
    Topic destination;

    @Override public UpdateAgent build() {
        return new ConfiguredUpdateAgent(applicationParameters, this);
    }
}

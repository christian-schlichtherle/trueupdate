/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.agent.ejb;

import javax.annotation.concurrent.Immutable;
import net.java.trueupdate.agent.spec.UpdateAgent;
import net.java.trueupdate.agent.spec.UpdateRuntimeException;

/**
 * A basic agent for updating this application.
 *
 * @author Christian Schlichtherle
 */
@Immutable
abstract class BasicUpdateAgent extends UpdateAgent {

    @Override public void subscribe() throws UpdateRuntimeException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override public void unsubscribe() throws UpdateRuntimeException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override public void install(String version) throws UpdateRuntimeException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}

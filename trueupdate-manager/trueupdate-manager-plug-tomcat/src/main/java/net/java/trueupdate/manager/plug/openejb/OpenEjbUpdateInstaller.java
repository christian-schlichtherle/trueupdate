/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.plug.openejb;

import javax.annotation.concurrent.Immutable;
import javax.ejb.EJB;
import net.java.trueupdate.manager.core.*;
import net.java.trueupdate.manager.spec.UpdateMessage;
import org.apache.openejb.assembler.Deployer;

/**
 * Installs updates for applications running in OpenEJB.
 *
 * @author Christian Schlichtherle
 */
@Immutable
public class OpenEjbUpdateInstaller implements UpdateInstaller {

    private @EJB Deployer deployer;

    @Override
    public void install(UpdateResolver resolver, UpdateMessage message)
    throws Exception {
        new ConfiguredOpenEjbUpdateInstaller(deployer, message)
                .install(resolver);
    }
}

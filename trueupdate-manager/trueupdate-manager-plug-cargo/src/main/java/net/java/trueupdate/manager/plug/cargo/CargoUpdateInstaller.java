/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.plug.cargo;

import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;
import net.java.trueupdate.manager.core.*;
import net.java.trueupdate.manager.spec.UpdateMessage;
import net.java.trueupdate.shed.Objects;
import org.codehaus.cargo.container.deployer.Deployer;

/**
 * Installs updates for applications running in OpenEJB.
 *
 * @author Christian Schlichtherle
 */
@Immutable
public class CargoUpdateInstaller implements UpdateInstaller {

    private final Deployer deployer;

    @Inject
    public CargoUpdateInstaller(final Deployer deployer) {
        this.deployer = Objects.requireNonNull(deployer);
    }

    @Override
    public void install(UpdateResolver resolver, UpdateMessage message)
    throws Exception {
        new ConfiguredCargoUpdateInstaller(deployer, message)
                .install(resolver);
    }
}

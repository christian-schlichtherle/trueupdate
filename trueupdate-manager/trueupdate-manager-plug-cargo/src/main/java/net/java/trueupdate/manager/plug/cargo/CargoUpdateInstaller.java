/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.plug.cargo;

import javax.annotation.concurrent.Immutable;
import net.java.trueupdate.manager.core.*;
import net.java.trueupdate.manager.spec.UpdateMessage;

/**
 * Installs updates for applications running in a container which is supported
 * by Cargo.
 *
 * @author Christian Schlichtherle
 */
@Immutable
public class CargoUpdateInstaller implements UpdateInstaller {

    @Override
    public void install(UpdateResolver resolver, UpdateMessage message)
    throws Exception {
        new ConfiguredCargoUpdateInstaller(message).install(resolver);
    }
}

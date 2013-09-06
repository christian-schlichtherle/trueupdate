/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.plug.cargo;

import java.io.File;
import java.net.URI;
import java.util.*;
import javax.annotation.concurrent.Immutable;
import net.java.trueupdate.manager.core.*;
import net.java.trueupdate.manager.core.tx.Transaction;
import net.java.trueupdate.manager.spec.UpdateMessage;

/**
 * Installs updates for applications running in a container which is supported
 * by Cargo.
 *
 * @author Christian Schlichtherle
 */
@Immutable
public final class CargoUpdateInstaller implements UpdateInstaller {

    @Override public void install(final UpdateResolver resolver,
                                  final UpdateMessage message)
    throws Exception {

        class ConfiguredUpdateInstaller extends LocalUpdateInstaller {

            final Map<URI, CargoContext> contexts = contexts(message);

            @Override protected File resolvePath(URI location) throws Exception {
                return context(location).resolvePath();
            }

            @Override protected Transaction deploymentTx(URI location) {
                return context(location).deploymentTx();
            }

            @Override protected Transaction undeploymentTx(URI location) {
                return context(location).undeploymentTx();
            }

            CargoContext context(URI location) {
                return contexts.get(location);
            }
        } // ConfiguredUpdateInstaller

        new ConfiguredUpdateInstaller().install(resolver, message);
    }

    static Map<URI, CargoContext> contexts(UpdateMessage message) {
        final URI currentLocation = message.currentLocation();
        final CargoContext currentContext = new CargoContext(currentLocation);
        final URI updateLocation = message.updateLocation();
        final Map<URI, CargoContext> contexts = new HashMap<URI, CargoContext>();
        contexts.put(currentLocation, currentContext);
        contexts.put(updateLocation, currentLocation.equals(updateLocation)
                ? currentContext : new CargoContext(updateLocation));
        return contexts;
    }
}

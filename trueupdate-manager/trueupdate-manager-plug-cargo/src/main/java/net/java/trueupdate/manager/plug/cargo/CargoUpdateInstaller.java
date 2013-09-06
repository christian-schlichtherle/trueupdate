/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.plug.cargo;

import java.io.File;
import java.net.URI;
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

            @Override protected Context resolveContext(final URI location) throws Exception {

                final CargoContext context = new CargoContext(location);
                final File path = context.resolvePath();

                class ResolvedContext implements Context {

                    @Override public File path() { return path; }

                    @Override public Transaction deploymentTx() {
                        return context.deploymentTx();
                    }

                    @Override public Transaction undeploymentTx() {
                        return context.undeploymentTx();
                    }
                } // ResolvedContext

                return new ResolvedContext();
            }
        } // ConfiguredUpdateInstaller

        new ConfiguredUpdateInstaller().install(resolver, message);
    }
}

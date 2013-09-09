/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.installer.cargo;

import java.io.File;
import java.net.URI;
import javax.annotation.concurrent.Immutable;
import net.java.trueupdate.installer.core.LocalUpdateInstaller;
import net.java.trueupdate.installer.core.tx.Transaction;
import net.java.trueupdate.manager.spec.UpdateMessage;

/**
 * Installs updates for applications running in a container which is supported
 * by Cargo.
 *
 * @author Christian Schlichtherle
 */
@Immutable
public final class CargoUpdateInstaller extends LocalUpdateInstaller {

    @Override
    protected Context resolveContext(final UpdateMessage message,
                                     final URI location)
    throws Exception {

        final CargoContext context = new CargoContext(location);
        final File path = context.deployablePath();

        class ResolvedContext implements Context {

            @Override public File path() { return path; }

            @Override public Transaction deploymentTransaction() {
                return context.deploymentTransaction();
            }

            @Override public Transaction undeploymentTransaction() {
                return context.undeploymentTransaction();
            }
        } // ResolvedContext

        return new ResolvedContext();
    }
}

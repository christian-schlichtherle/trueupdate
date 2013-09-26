/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.installer.cargo;

import java.io.File;
import java.net.URI;
import javax.annotation.concurrent.Immutable;
import net.java.trueupdate.installer.core.LocalUpdateInstaller;
import net.java.trueupdate.installer.core.tx.Transaction;
import net.java.trueupdate.manager.spec.UpdateContext;
import net.java.trueupdate.message.UpdateMessage;

/**
 * Installs updates for applications running in a container which is supported
 * by Cargo.
 *
 * @author Christian Schlichtherle
 */
@Immutable
public final class CargoUpdateInstaller extends LocalUpdateInstaller {

    @Override
    protected LocationContext locationContext(final UpdateContext _,
                                              final String location)
    throws Exception {

        final CargoContext context = new CargoContext(new URI(location));
        final File path = context.deployablePath();

        class ResolvedLocationContext implements LocationContext {

            @Override public File path() { return path; }

            @Override public Transaction undeploymentTransaction() {
                return context.undeploymentTransaction();
            }

            @Override public Transaction deploymentTransaction() {
                return context.deploymentTransaction();
            }
        } // ResolvedLocationContext

        return new ResolvedLocationContext();
    }
}

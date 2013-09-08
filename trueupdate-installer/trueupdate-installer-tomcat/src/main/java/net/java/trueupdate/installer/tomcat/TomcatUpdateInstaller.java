/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.installer.tomcat;

import java.net.URI;
import javax.annotation.concurrent.Immutable;
import net.java.trueupdate.manager.core.*;
import net.java.trueupdate.manager.spec.UpdateMessage;

/**
 * Installs updates for applications running in OpenEJB.
 *
 * @author Christian Schlichtherle
 */
@Immutable
public final class TomcatUpdateInstaller extends LocalUpdateInstaller {

    @Override
    protected Context resolveContext(final UpdateMessage message,
                                     final URI location)
    throws Exception {
        throw new UnsupportedOperationException();
    }
}

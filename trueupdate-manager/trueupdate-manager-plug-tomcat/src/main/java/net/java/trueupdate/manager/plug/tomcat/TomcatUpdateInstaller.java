/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.plug.tomcat;

import javax.annotation.concurrent.Immutable;
import net.java.trueupdate.manager.spec.*;

/**
 * Installs updates for applications running in Tomcat.
 *
 * @author Christian Schlichtherle
 */
@Immutable
public class TomcatUpdateInstaller implements UpdateInstaller {

    @Override
    public void install(UpdateResolver resolver, UpdateMessage message)
    throws Exception {
        new ConfiguredTomcatUpdateInstaller(message).install(resolver);
    }
}

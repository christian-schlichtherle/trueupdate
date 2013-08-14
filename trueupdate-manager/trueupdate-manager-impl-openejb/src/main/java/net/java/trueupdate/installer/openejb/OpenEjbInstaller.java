/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.installer.openejb;

import javax.ejb.Stateless;
import net.java.trueupdate.manager.spec.*;

/**
 * Installs updates for applications running in an OpenEJB container.
 *
 * @author Christian Schlichtherle
 */
@Stateless
public class OpenEjbInstaller implements UpdateInstaller {

    @Override public void install(UpdateMessage message) throws Exception {
        throw new Exception("TODO");
    }
}

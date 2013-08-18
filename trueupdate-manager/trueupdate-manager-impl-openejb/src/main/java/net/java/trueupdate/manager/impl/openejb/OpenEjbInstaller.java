/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.impl.openejb;

import java.util.logging.*;
import javax.ejb.Stateless;
import net.java.trueupdate.manager.spec.*;

/**
 * Installs updates for applications running in an OpenEJB container.
 *
 * @author Christian Schlichtherle
 */
@Stateless
public class OpenEjbInstaller implements UpdateInstaller {

    private static final Logger
            logger = Logger.getLogger(OpenEjbInstaller.class.getName());

    @Override public void install(UpdateMessage message) throws Exception {
        logger.log(Level.INFO, "TODO: Implement the installation of:\n{0}",
                message);
    }
}

/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.installer.openejb;

import java.util.logging.Logger;
import javax.inject.Inject;
import net.java.trueupdate.manager.spec.UpdateInstaller;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Christian Schlichtherle
 */
@RunWith(Arquillian.class)
public class OpenEjbUpdateInstallerIT {

    private static final Logger
            logger = Logger.getLogger(OpenEjbUpdateInstallerIT.class.getName());

    public static @Deployment WebArchive createDeployment() {
        final WebArchive archive = ShrinkWrap
                .create(WebArchive.class)
                .addClass(OpenEjbUpdateInstaller.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
        logger.config(archive.toString(true));
        return archive;
    }

    private @Inject UpdateInstaller installer;

    public @Test void testInjection() {
        assert installer instanceof OpenEjbUpdateInstaller;
    }
}
